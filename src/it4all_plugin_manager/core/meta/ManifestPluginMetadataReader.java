package it4all_plugin_manager.core.meta;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ManifestPluginMetadataReader implements PluginMetadataReader {

	@Override
	public Optional<String> readRealName(Path pluginPath) {
		if (Files.isDirectory(pluginPath)) {
			return readFromDirectory(pluginPath);
		}
		return readFromJar(pluginPath);
	}

	private Optional<String> readFromDirectory(Path directory) {
		Path manifestPath = directory.resolve("META-INF").resolve("MANIFEST.MF");
		if (!Files.exists(manifestPath)) {
			return Optional.empty();
		}

		try (InputStream inputStream = Files.newInputStream(manifestPath)) {
			Manifest manifest = new Manifest(inputStream);
			return Optional.ofNullable(manifest.getMainAttributes().getValue("Bundle-Name"));
		} catch (IOException exception) {
			return Optional.empty();
		}
	}

	private Optional<String> readFromJar(Path jarPath) {
		if (!jarPath.getFileName().toString().endsWith(".jar")) {
			return Optional.empty();
		}

		try (JarFile jar = new JarFile(jarPath.toFile())) {
			Manifest manifest = jar.getManifest();
			if (manifest == null) {
				return Optional.empty();
			}
			return Optional.ofNullable(manifest.getMainAttributes().getValue("Bundle-Name"));
		} catch (IOException exception) {
			return Optional.empty();
		}
	}
}
