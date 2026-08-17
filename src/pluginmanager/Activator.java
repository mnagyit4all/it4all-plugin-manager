package pluginmanager;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import pluginmanager.service.PluginLifecycleService;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "pluginmanager";
    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        // Indításkori csekkolás és állapotok szinkronizálása
        PluginLifecycleService lifecycleService = new PluginLifecycleService(context, getStateLocation().toFile());
        lifecycleService.processStartupPlugins();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Visszaadja a shared instance-t
     */
    public static Activator getDefault() {
        return plugin;
    }
}