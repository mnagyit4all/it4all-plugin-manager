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

		String content = Files.readString(registryPath, StandardCharsets.UTF_8).trim();
		if (content.isEmpty()) {
			return List.of();
		}
		if (!content.startsWith("[") || !content.endsWith("]")) {
			throw new IOException("Registry format is invalid.");
		}

		List<PluginRecord> records = new ArrayList<>();
		int index = 0;
		Matcher matcher = RECORD_PATTERN.matcher(content);
		while (matcher.find()) {
			validateAllowedSegment(content.substring(index, matcher.start()));
			records.add(new PluginRecord(
				unescape(matcher.group(1)),
				unescape(matcher.group(2)),
				PluginState.valueOf(matcher.group(3))));
			index = matcher.end();
		}
		validateAllowedSegment(content.substring(index));

		if (!records.isEmpty()) {
			return records;
		}
		if ("[]".equals(content.replaceAll("\\s+", ""))) {
			return records;
		}
		throw new IOException("Registry format is invalid or corrupted.");
	}

	private void validateAllowedSegment(String segment) throws IOException {
		String normalized = segment.replaceAll("\\s+", "");
		if (normalized.isEmpty() || "[".equals(normalized) || "]".equals(normalized)
			|| ",[".equals(normalized) || "],".equals(normalized) || ",".equals(normalized)) {
			return;
		}
		if (!normalized.matches("^[\\[\\],]*$")) {
			throw new IOException("Registry contains invalid content.");
		}
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

	private String unescape(String value) {
		StringBuilder builder = new StringBuilder();
		boolean escaping = false;
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (escaping) {
				builder.append(current);
				escaping = false;
				continue;
			}
			if (current == '\\') {
				escaping = true;
				continue;
			}
			builder.append(current);
		}
		if (escaping) {
			builder.append('\\');
		}
		return builder.toString();
	}
}
