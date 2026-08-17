package pluginmanager.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;
import pluginmanager.Activator;
import pluginmanager.model.PluginItem;
import pluginmanager.service.BundleValidationService;
import pluginmanager.service.ConfigService;
import pluginmanager.service.StorageService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PluginManagerView extends ViewPart {

    public static final String ID = "pluginmanager.ui.PluginManagerView";

    private TableViewer tableViewer;
    private Button importButton;
    private Button toggleButton;

    private ConfigService configService;
    private StorageService storageService;
    private BundleValidationService validationService;
    private List<PluginItem> pluginList;

    @Override
    public void createPartControl(Composite parent) {
        File baseDir = Activator.getDefault().getStateLocation().toFile();
        this.configService = new ConfigService(baseDir);
        this.storageService = new StorageService(baseDir);
        this.validationService = new BundleValidationService();

        this.pluginList = configService.loadConfig();

        GridLayout layout = new GridLayout(2, false);
        parent.setLayout(layout);

        createTableViewer(parent);
        createButtonArea(parent);

        tableViewer.setInput(pluginList);
    }

    private void createTableViewer(Composite parent) {
        Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        TableColumn colName = new TableColumn(table, SWT.LEFT);
        colName.setText("Plugin Név / Version");
        colName.setWidth(300);

        TableColumn colStatus = new TableColumn(table, SWT.CENTER);
        colStatus.setText("Státusz");
        colStatus.setWidth(120);

        tableViewer = new TableViewer(table);
        tableViewer.setContentProvider(new PluginContentProvider());
        tableViewer.setLabelProvider(new PluginLabelProvider());

        tableViewer.addSelectionChangedListener(event -> {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            boolean hasSelection = !sel.isEmpty();
            toggleButton.setEnabled(hasSelection);
            if (hasSelection) {
                PluginItem item = (PluginItem) sel.getFirstElement();
                toggleButton.setText(item.isEnabled() ? "Disable" : "Enable");
            }
        });
    }

    private void createButtonArea(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        buttonComp.setLayout(new GridLayout(1, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

        importButton = new Button(buttonComp, SWT.PUSH);
        importButton.setText("Import Plugin...");
        importButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        importButton.addListener(SWT.Selection, e -> handleImport());

        toggleButton = new Button(buttonComp, SWT.PUSH);
        toggleButton.setText("Enable / Disable");
        toggleButton.setEnabled(false);
        toggleButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        toggleButton.addListener(SWT.Selection, e -> handleToggle());
    }

    private void handleImport() {
        FileDialog dialog = new FileDialog(getSite().getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[]{"*.jar"});
        dialog.setFilterNames(new String[]{"Java Archive (*.jar)"});

        String selectedPath = dialog.open();
        if (selectedPath == null) return;

        File sourceJar = new File(selectedPath);
        BundleValidationService.ValidationResult result = validationService.validateBundle(sourceJar);

        if (!result.isValid()) {
            MessageDialog.openError(getSite().getShell(), "Importálási hiba", result.getErrorMessage());
            return;
        }

        PluginItem newItem = new PluginItem(result.getSymbolicName(), result.getVersion(), "", false);

        int existingIndex = pluginList.indexOf(newItem);
        if (existingIndex >= 0) {
            boolean overwrite = MessageDialog.openQuestion(
                    getSite().getShell(),
                    "Összeütközés",
                    "A(z) " + newItem.getSymbolicName() + " (" + newItem.getVersion() + ") már létezik. Felülírod?"
            );
            if (!overwrite) return;
        }

        try {
            String relativeJarPath = storageService.copyJarToStorage(sourceJar);
            newItem.setJarPath(relativeJarPath);

            if (existingIndex >= 0) {
                pluginList.set(existingIndex, newItem);
            } else {
                pluginList.add(newItem);
            }

            configService.saveConfig(pluginList);
            tableViewer.refresh();

            MessageDialog.openInformation(
                    getSite().getShell(),
                    "Sikeres importálás",
                    "A plugin importálva lett DISABLED státusszal.\nA változtatások az Eclipse újraindítása után lépnek életbe."
            );

        } catch (IOException ex) {
            MessageDialog.openError(getSite().getShell(), "Hiba", "Nem sikerült a JAR fájlt másolni: " + ex.getMessage());
        }
    }

    private void handleToggle() {
        IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
        if (sel.isEmpty()) return;

        PluginItem selectedItem = (PluginItem) sel.getFirstElement();
        boolean newStatus = !selectedItem.isEnabled();
        selectedItem.setEnabled(newStatus);

        File jarFile = storageService.getAbsoluteFile(selectedItem.getJarPath());
        try {
            // Szinkronizálás a dropins mappával
            storageService.syncWithDropins(jarFile, newStatus);
        } catch (IOException e) {
            MessageDialog.openError(getSite().getShell(), "Hiba", "Nem sikerült frissíteni a dropins mappát: " + e.getMessage());
            return;
        }

        configService.saveConfig(pluginList);
        tableViewer.refresh();

        toggleButton.setText(selectedItem.isEnabled() ? "Disable" : "Enable");

        MessageDialog.openWarning(
                getSite().getShell(),
                "Újraindítás szükséges",
                "A plugin állapota módosult.\nAz UI elemek (nézetek, menük) megjelenítéséhez indítsd újra az Eclipse-t!"
        );
    }

    @Override
    public void setFocus() {
        tableViewer.getControl().setFocus();
    }
}