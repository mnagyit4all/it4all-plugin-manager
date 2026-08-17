package pluginmanager.service;

import org.osgi.framework.BundleContext;
import pluginmanager.model.PluginItem;

import java.io.File;
import java.util.List;

public class PluginLifecycleService {

    private final ConfigService configService;
    private final StorageService storageService;

    public PluginLifecycleService(BundleContext context, File baseDir) {
        this.configService = new ConfigService(baseDir);
        this.storageService = new StorageService(baseDir);
    }

    /**
     * Indításkor szinkronizálja a tárolt konfigurációt a fizikai dropins filmmel
     */
    public void processStartupPlugins() {
        List<PluginItem> items = configService.loadConfig();
        for (PluginItem item : items) {
            File jarFile = storageService.getAbsoluteFile(item.getJarPath());
            if (!jarFile.exists()) continue;

            try {
                // A fizikai dropins mappába másoljuk vagy töröljük a fájlt
                storageService.syncWithDropins(jarFile, item.isEnabled());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}