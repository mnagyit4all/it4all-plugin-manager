package it4all_plugin_manager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import it4all_plugin_manager.core.service.PluginManagerService;
import it4all_plugin_manager.lifecycle.PluginManagerBootstrap;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "it4all-plugin-manager"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private PluginManagerBootstrap bootstrap;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bootstrap = new PluginManagerBootstrap();
		try {
			bootstrap.initialize();
		} catch (Exception exception) {
			getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Plugin manager bootstrap failed.", exception));
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		bootstrap = null;
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public PluginManagerService getPluginManagerService() {
		if (bootstrap == null) {
			throw new IllegalStateException("Plugin manager bootstrap is not initialized.");
		}
		return bootstrap.getPluginManagerService();
	}

}
