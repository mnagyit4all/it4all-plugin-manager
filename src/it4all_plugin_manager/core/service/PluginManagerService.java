package it4all_plugin_manager.core.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import it4all_plugin_manager.core.config.EclipsePaths;
import it4all_plugin_manager.core.discovery.TempPluginScanner;
import it4all_plugin_manager.core.io.CollisionPolicy;
import it4all_plugin_manager.core.io.PluginFileMover;
import it4all_plugin_manager.core.model.PluginRecord;
import it4all_plugin_manager.core.model.PluginState;
import it4all_plugin_manager.core.registry.PluginRegistryStore;

public class PluginManagerService {
	private static final Logger LOGGER = Logger.getLogger(PluginManagerService.class.getName());

	private final EclipsePaths eclipsePaths;
	private final PluginRegistryStore registryStore;
	private final TempPluginScanner tempPluginScanner;
	private final PluginFileMover fileMover;
	private final CollisionPolicy collisionPolicy;

	public PluginManagerService(
		EclipsePaths eclipsePaths,
		PluginRegistryStore registryStore,
		TempPluginScanner tempPluginScanner,
		PluginFileMover fileMover,
		CollisionPolicy collisionPolicy) {
		this.eclipsePaths = eclipsePaths;
		this.registryStore = registryStore;
		this.tempPluginScanner = tempPluginScanner;
		this.fileMover = fileMover;
		this.collisionPolicy = collisionPolicy;
	}

	public void initialize() throws IOException {
		Files.createDirectories(eclipsePaths.getTemp());
		LOGGER.info(() -> "Ensured temp directory: " + eclipsePaths.getTemp());

		List<PluginRecord> scannedFromTemp = tempPluginScanner.scan(eclipsePaths.getTemp());
		LOGGER.info(() -> "Temp scan completed. Found entries: " + scannedFromTemp.size());

		List<PluginRecord> registry;
		try {
			registry = registryStore.load();
		} catch (Exception exception) {
			LOGGER.log(Level.WARNING, "Registry is broken. Creating backup and resetting.", exception);
			registryStore.backupAndResetBrokenRegistry();
			registry = List.of();
		}

		List<PluginRecord> merged = registryStore.upsertTempSeen(registry, scannedFromTemp);
		List<PluginRecord> repaired = reconcileRegistryStates(merged);
		registryStore.save(repaired);
		LOGGER.info(() -> "Registry initialized with entries: " + repaired.size());
	}

	public List<PluginRecord> getRegisteredPlugins() throws IOException {
		return registryStore.load();
	}

	public List<PluginRecord> applyStates(Map<String, PluginState> desiredStateByFileName) throws IOException {
		List<PluginRecord> registry = new ArrayList<>(registryStore.load());
		Map<String, PluginRecord> byFileName = new LinkedHashMap<>();
		for (PluginRecord record : registry) {
			byFileName.put(record.getFileName(), record);
		}
		List<MoveOperation> completedMoves = new ArrayList<>();

		try {
			for (Map.Entry<String, PluginState> entry : desiredStateByFileName.entrySet()) {
				PluginRecord current = byFileName.get(entry.getKey());
				if (current == null) {
					LOGGER.warning(() -> "Requested file is not registered and will be ignored: " + entry.getKey());
					continue;
				}
				if (current.getState() == entry.getValue()) {
					continue;
				}

				boolean moved = moveByState(current.getFileName(), current.getState(), entry.getValue());
				if (moved) {
					completedMoves.add(new MoveOperation(current.getFileName(), current.getState(), entry.getValue()));
					current.setState(entry.getValue());
					LOGGER.info(() -> "State changed for " + current.getFileName() + ": " + entry.getValue());
				} else {
					LOGGER.warning(() -> "Move skipped for " + current.getFileName() + " using policy " + collisionPolicy);
				}
			}

			List<PluginRecord> result = reconcileRegistryStates(new ArrayList<>(byFileName.values()));
			registryStore.save(result);
			return result;
		} catch (IOException exception) {
			LOGGER.log(Level.SEVERE, "File operation failed. Rolling back previously moved items.", exception);
			rollbackMoves(completedMoves);
			List<PluginRecord> repaired = reconcileRegistryStates(new ArrayList<>(byFileName.values()));
			registryStore.save(repaired);
			throw exception;
		}
	}

	private boolean moveByState(String fileName, PluginState from, PluginState to) throws IOException {
		Path source = resolvePath(fileName, from);
		Path target = resolvePath(fileName, to);
		LOGGER.info(() -> "Moving plugin " + fileName + " from " + source + " to " + target);
		return fileMover.move(source, target, collisionPolicy);
	}

	private void rollbackMoves(List<MoveOperation> completedMoves) {
		Collections.reverse(completedMoves);
		for (MoveOperation operation : completedMoves) {
			try {
				Path source = resolvePath(operation.fileName, operation.to);
				Path target = resolvePath(operation.fileName, operation.from);
				LOGGER.warning(() -> "Rollback move for " + operation.fileName + " from " + source + " to " + target);
				fileMover.move(source, target, CollisionPolicy.OVERWRITE);
			} catch (Exception exception) {
				LOGGER.log(Level.SEVERE, "Rollback failed for " + operation.fileName, exception);
			}
		}
	}

	private List<PluginRecord> reconcileRegistryStates(List<PluginRecord> records) {
		for (PluginRecord record : records) {
			PluginState filesystemState = resolveStateFromFilesystem(record.getFileName(), record.getState());
			if (filesystemState != record.getState()) {
				LOGGER.warning(() -> "Repairing registry state for " + record.getFileName() + " from " + record.getState() + " to " + filesystemState);
				record.setState(filesystemState);
			}
		}
		return records;
	}

	private PluginState resolveStateFromFilesystem(String fileName, PluginState fallback) {
		boolean inDropins = Files.exists(eclipsePaths.getDropins().resolve(fileName));
		boolean inTemp = Files.exists(eclipsePaths.getTemp().resolve(fileName));

		if (inDropins && !inTemp) {
			return PluginState.DROPINS;
		}
		if (!inDropins && inTemp) {
			return PluginState.TEMP;
		}
		if (inDropins && inTemp) {
			LOGGER.warning(() -> "Plugin exists in both locations, preferring DROPINS: " + fileName);
			return PluginState.DROPINS;
		}

		LOGGER.warning(() -> "Plugin file missing from both locations, keeping previous state: " + fileName);
		return fallback;
	}

	private Path resolvePath(String fileName, PluginState state) {
		if (state == PluginState.DROPINS) {
			return eclipsePaths.getDropins().resolve(fileName);
		}
		return eclipsePaths.getTemp().resolve(fileName);
	}

	private static class MoveOperation {
		private final String fileName;
		private final PluginState from;
		private final PluginState to;

		private MoveOperation(String fileName, PluginState from, PluginState to) {
			this.fileName = fileName;
			this.from = from;
			this.to = to;
		}
	}
}
