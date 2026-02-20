package it4all_plugin_manager.core.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it4all_plugin_manager.core.config.EclipsePaths;
import it4all_plugin_manager.core.discovery.TempPluginScanner;
import it4all_plugin_manager.core.io.CollisionPolicy;
import it4all_plugin_manager.core.io.PluginFileMover;
import it4all_plugin_manager.core.model.PluginRecord;
import it4all_plugin_manager.core.model.PluginState;
import it4all_plugin_manager.core.registry.PluginRegistryStore;

public class PluginManagerService {
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
		List<PluginRecord> scannedFromTemp = tempPluginScanner.scan(eclipsePaths.getTemp());
		List<PluginRecord> registry;
		try {
			registry = registryStore.load();
		} catch (Exception exception) {
			registryStore.backupAndResetBrokenRegistry();
			registry = List.of();
		}

		List<PluginRecord> merged = registryStore.upsertTempSeen(registry, scannedFromTemp);
		registryStore.save(merged);
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

		for (Map.Entry<String, PluginState> entry : desiredStateByFileName.entrySet()) {
			PluginRecord current = byFileName.get(entry.getKey());
			if (current == null) {
				continue;
			}
			if (current.getState() == entry.getValue()) {
				continue;
			}

			boolean moved = moveByState(current.getFileName(), current.getState(), entry.getValue());
			if (moved) {
				current.setState(entry.getValue());
			}
		}

		List<PluginRecord> result = new ArrayList<>(byFileName.values());
		registryStore.save(result);
		return result;
	}

	private boolean moveByState(String fileName, PluginState from, PluginState to) throws IOException {
		Path source = resolvePath(fileName, from);
		Path target = resolvePath(fileName, to);
		return fileMover.move(source, target, collisionPolicy);
	}

	private Path resolvePath(String fileName, PluginState state) {
		if (state == PluginState.DROPINS) {
			return eclipsePaths.getDropins().resolve(fileName);
		}
		return eclipsePaths.getTemp().resolve(fileName);
	}
}
