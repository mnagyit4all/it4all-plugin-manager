package it4all_plugin_manager.core.discovery;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import it4all_plugin_manager.core.meta.PluginMetadataReader;
import it4all_plugin_manager.core.model.PluginRecord;
import it4all_plugin_manager.core.model.PluginState;

public class TempPluginScanner {
	private final PluginMetadataReader metadataReader;

	public TempPluginScanner(PluginMetadataReader metadataReader) {
		this.metadataReader = metadataReader;
	}

	public List<PluginRecord> scan(Path tempDirectory) throws IOException {
		if (!Files.exists(tempDirectory)) {
			return List.of();
		}

		List<PluginRecord> records = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDirectory)) {
			for (Path entry : stream) {
				if (!isPluginEntry(entry)) {
					continue;
				}
				String fileName = entry.getFileName().toString();
				String realName = metadataReader.readRealName(entry).orElse(fileName);
				records.add(new PluginRecord(fileName, realName, PluginState.TEMP));
			}
		}

		return records;
	}

	private boolean isPluginEntry(Path entry) {
		if (Files.isDirectory(entry)) {
			return true;
		}
		String fileName = entry.getFileName().toString().toLowerCase();
		return fileName.endsWith(".jar");
	}
}
