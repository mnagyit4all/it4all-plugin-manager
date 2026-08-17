package pluginmanager.service;

import pluginmanager.model.PluginItem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigService {

    private final File configFile;

    public ConfigService(File baseDir) {
        this.configFile = new File(baseDir, "config.json");
    }

    public synchronized List<PluginItem> loadConfig() {
        List<PluginItem> list = new ArrayList<>();
        if (!configFile.exists()) {
            return list;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return list;
        }

        String json = sb.toString().trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1).trim();
            if (json.isEmpty()) return list;

            String[] objects = json.split("(?<=\\}),\\s*(?=\\{)");
            for (String objStr : objects) {
                PluginItem item = parsePluginItem(objStr);
                if (item != null) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    public synchronized void saveConfig(List<PluginItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < items.size(); i++) {
            PluginItem item = items.get(i);
            sb.append("  {\n");
            sb.append("    \"symbolicName\": \"").append(escape(item.getSymbolicName())).append("\",\n");
            sb.append("    \"version\": \"").append(escape(item.getVersion())).append("\",\n");
            sb.append("    \"jarPath\": \"").append(escape(item.getJarPath())).append("\",\n");
            sb.append("    \"enabled\": ").append(item.isEnabled()).append("\n");
            sb.append("  }");
            if (i < items.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PluginItem parsePluginItem(String jsonObj) {
        PluginItem item = new PluginItem();
        String clean = jsonObj.replace("{", "").replace("}", "");
        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String val = kv[1].trim().replace("\"", "");
                switch (key) {
                    case "symbolicName": item.setSymbolicName(val); break;
                    case "version": item.setVersion(val); break;
                    case "jarPath": item.setJarPath(val); break;
                    case "enabled": item.setEnabled(Boolean.parseBoolean(val)); break;
                }
            }
        }
        return item;
    }

    private String escape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}