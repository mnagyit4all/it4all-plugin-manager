package it4all_plugin_manager.core.meta;

import java.nio.file.Path;
import java.util.Optional;

public interface PluginMetadataReader {
	Optional<String> readRealName(Path pluginPath);
}
