package it4all_plugin_manager.core.registry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it4all_plugin_manager.core.model.PluginRecord;
import it4all_plugin_manager.core.model.PluginState;

public class PluginRegistryStore {
	private static final Pattern RECORD_PATTERN = Pattern.compile("\\{\\s*\\\"fileName\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"\\s*,\\s*\\\"realName\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"\\s*,\\s*\\\"state\\\"\\s*:\\s*\\\"(TEMP|DROPINS)\\\"\\s*\\}");
	private final Path registryPath;

	public PluginRegistryStore(Path registryPath) {
		this.registryPath = registryPath;
	}

	public List<PluginRecord> load() throws IOException {
		if (!Files.exists(registryPath)) {
			return List.of();
		}

		String content = Files.readString(registryPath, StandardCharsets.UTF_8);
		List<PluginRecord> records = new ArrayList<>();
		Matcher matcher = RECORD_PATTERN.matcher(content);
		while (matcher.find()) {
			records.add(new PluginRecord(
				matcher.group(1),
				matcher.group(2),
				PluginState.valueOf(matcher.group(3))));
		}
		return records;
	}

	public void save(List<PluginRecord> records) throws IOException {
		Files.createDirectories(registryPath.getParent());
		Files.writeString(registryPath, toJson(records), StandardCharsets.UTF_8);
	}

	public List<PluginRecord> upsertTempSeen(List<PluginRecord> existing, List<PluginRecord> scannedFromTemp) {
		Map<String, PluginRecord> byFileName = new LinkedHashMap<>();
		for (PluginRecord record : existing) {
			byFileName.put(record.getFileName(), record);
		}
		for (PluginRecord scanned : scannedFromTemp) {
			PluginRecord current = byFileName.get(scanned.getFileName());
			if (current == null) {
				byFileName.put(scanned.getFileName(), scanned);
			} else {
				String chosenRealName = scanned.getRealName().isBlank() ? current.getRealName() : scanned.getRealName();
				byFileName.put(scanned.getFileName(), new PluginRecord(scanned.getFileName(), chosenRealName, current.getState()));
			}
		}
		return new ArrayList<>(byFileName.values());
	}

	public void backupAndResetBrokenRegistry() throws IOException {
		if (!Files.exists(registryPath)) {
			return;
		}
		Path backupPath = registryPath.resolveSibling(registryPath.getFileName() + ".broken.bak");
		Files.copy(registryPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
		save(List.of());
	}

	private String toJson(List<PluginRecord> records) {
		StringBuilder builder = new StringBuilder();
		builder.append("[\n");
		for (int index = 0; index < records.size(); index++) {
			PluginRecord record = records.get(index);
			builder.append("  {");
			builder.append("\"fileName\":\"").append(escape(record.getFileName())).append("\",");
			builder.append("\"realName\":\"").append(escape(record.getRealName())).append("\",");
			builder.append("\"state\":\"").append(record.getState().name()).append("\"");
			builder.append("}");
			if (index < records.size() - 1) {
				builder.append(",");
			}
			builder.append("\n");
		}
		builder.append("]\n");
		return builder.toString();
	}

	private String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
