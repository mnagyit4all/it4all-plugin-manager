package it4all_plugin_manager.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import it4all_plugin_manager.Activator;
import it4all_plugin_manager.core.model.PluginRecord;

public class PluginManagerView extends ViewPart {

	public static final String VIEW_ID = "it4all_plugin_manager.views.PluginManagerView";

	private CheckboxTableViewer viewer;
	private PluginManagerPresenter presenter;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		if (!initializePresenter(parent)) {
			return;
		}

		createViewer(parent);
		createApplyButton(parent);
		loadItemsIntoViewer();
	}

	private boolean initializePresenter(Composite parent) {
		try {
			presenter = new PluginManagerPresenter(Activator.getDefault().getPluginManagerService());
			return true;
		} catch (Exception exception) {
			Label label = new Label(parent, SWT.WRAP);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("A Plugin Manager service nem elérhető. Ellenőrizd az Eclipse útvonal-felismerést és a logokat.");
			return false;
		}
	}

	private void createViewer(Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn realNameColumn = new TableViewerColumn(viewer, SWT.NONE);
		realNameColumn.getColumn().setText("Plugin");
		realNameColumn.getColumn().setWidth(280);
		realNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((PluginListItem) element).getRealName();
			}
		});

		TableViewerColumn fileNameColumn = new TableViewerColumn(viewer, SWT.NONE);
		fileNameColumn.getColumn().setText("Fájlnév");
		fileNameColumn.getColumn().setWidth(280);
		fileNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((PluginListItem) element).getFileName();
			}
		});
	}

	private void createApplyButton(Composite parent) {
		Button applyButton = new Button(parent, SWT.PUSH);
		applyButton.setText("Apply");
		applyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		applyButton.addListener(SWT.Selection, event -> applyChanges());
	}

	private void loadItemsIntoViewer() {
		try {
			List<PluginListItem> items = presenter.loadItems();
			viewer.setInput(items);
			for (PluginListItem item : items) {
				viewer.setChecked(item, item.isChecked());
			}
		} catch (IOException exception) {
			MessageDialog.openError(getSite().getShell(), "Plugin Manager", "Nem sikerült betölteni a plugin listát.");
		}
	}

	private void applyChanges() {
		Object[] elements = viewer.getCheckedElements();
		List<PluginListItem> allItems = getAllItemsFromInput();
		for (PluginListItem item : allItems) {
			item.setChecked(false);
		}
		for (Object element : elements) {
			((PluginListItem) element).setChecked(true);
		}

		try {
			List<PluginRecord> updated = presenter.apply(allItems);
			viewer.setInput(toViewItems(updated));
			MessageDialog.openInformation(
				getSite().getShell(),
				"Plugin Manager",
				"A módosítások alkalmazva. Az Eclipse újraindítása kötelező a konzisztens plugin állapothoz.");
			loadItemsIntoViewer();
		} catch (IOException exception) {
			MessageDialog.openError(getSite().getShell(), "Plugin Manager", "Hiba történt az alkalmazás közben.");
		}
	}

	@SuppressWarnings("unchecked")
	private List<PluginListItem> getAllItemsFromInput() {
		Object input = viewer.getInput();
		if (input instanceof List<?>) {
			return new ArrayList<>((List<PluginListItem>) input);
		}
		return List.of();
	}

	private List<PluginListItem> toViewItems(List<PluginRecord> records) {
		List<PluginListItem> items = new ArrayList<>();
		for (PluginRecord record : records) {
			items.add(PluginListItem.fromRecord(record));
		}
		return items;
	}

	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getTable().isDisposed()) {
			viewer.getTable().setFocus();
		}
	}
}
