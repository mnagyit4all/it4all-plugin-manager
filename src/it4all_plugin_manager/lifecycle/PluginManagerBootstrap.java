package it4all_plugin_manager.lifecycle;

import java.nio.file.Path;
import java.util.Optional;

import it4all_plugin_manager.core.config.EclipsePathResolver;
import it4all_plugin_manager.core.config.EclipsePaths;
import it4all_plugin_manager.core.discovery.TempPluginScanner;
import it4all_plugin_manager.core.io.CollisionPolicy;
import it4all_plugin_manager.core.io.PluginFileMover;
import it4all_plugin_manager.core.meta.ManifestPluginMetadataReader;
import it4all_plugin_manager.core.registry.PluginRegistryStore;
import it4all_plugin_manager.core.service.PluginManagerService;

public class PluginManagerBootstrap {

	private PluginManagerService pluginManagerService;

	public void initialize() throws Exception {
		EclipsePathResolver resolver = new EclipsePathResolver();
		Optional<EclipsePaths> resolved = resolver.resolve();
		if (resolved.isEmpty()) {
			throw new IllegalStateException("Unable to resolve Eclipse installation path.");
		}

		EclipsePaths eclipsePaths = resolved.get();
		Path registryPath = eclipsePaths.getTemp().resolve("plugin-manager-registry.json");
		pluginManagerService = new PluginManagerService(
			eclipsePaths,
			new PluginRegistryStore(registryPath),
			new TempPluginScanner(new ManifestPluginMetadataReader()),
			new PluginFileMover(),
			CollisionPolicy.SKIP);

		pluginManagerService.initialize();
	}

	public PluginManagerService getPluginManagerService() {
		if (pluginManagerService == null) {
			throw new IllegalStateException("PluginManagerService is not initialized.");
		}
		return pluginManagerService;
	}
}
