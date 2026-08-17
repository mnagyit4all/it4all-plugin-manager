package pluginmanager.ui;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import pluginmanager.model.PluginItem;

public class PluginLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (!(element instanceof PluginItem)) return "";

        PluginItem item = (PluginItem) element;
        switch (columnIndex) {
            case 0:
                return item.getSymbolicName() + " (" + item.getVersion() + ")";
            case 1:
                return item.isEnabled() ? "ENABLED" : "DISABLED";
            default:
                return "";
        }
    }
}