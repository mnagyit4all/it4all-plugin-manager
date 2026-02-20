package it4all_plugin_manager.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenPluginManagerViewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null || window.getActivePage() == null) {
			return null;
		}

		try {
			window.getActivePage().showView(PluginManagerView.VIEW_ID);
		} catch (PartInitException exception) {
			MessageDialog.openError(window.getShell(), "Plugin Manager", "Nem sikerült megnyitni a Plugin Manager nézetet.");
			throw new ExecutionException("Failed to open PluginManagerView", exception);
		}

		return null;
	}
}
