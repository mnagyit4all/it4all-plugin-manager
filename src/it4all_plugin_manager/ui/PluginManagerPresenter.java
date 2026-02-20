package it4all_plugin_manager.ui;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it4all_plugin_manager.core.model.PluginRecord;
import it4all_plugin_manager.core.model.PluginState;
import it4all_plugin_manager.core.service.PluginManagerService;

public class PluginManagerPresenter {
	private final PluginManagerService pluginManagerService;

	public PluginManagerPresenter(PluginManagerService pluginManagerService) {
		this.pluginManagerService = pluginManagerService;
	}

	public List<PluginListItem> loadItems() throws IOException {
		List<PluginRecord> records = pluginManagerService.getRegisteredPlugins();
		return records.stream().map(PluginListItem::fromRecord).collect(Collectors.toList());
	}

	public List<PluginRecord> apply(List<PluginListItem> items) throws IOException {
		Map<String, PluginState> currentStateByFileName = new LinkedHashMap<>();
		for (PluginRecord record : pluginManagerService.getRegisteredPlugins()) {
			currentStateByFileName.put(record.getFileName(), record.getState());
		}

		Map<String, PluginState> desiredStateByFileName = new LinkedHashMap<>();
		for (PluginListItem item : items) {
			PluginState desired = item.toDesiredState();
			PluginState current = currentStateByFileName.get(item.getFileName());
			if (current != null && current != desired) {
				desiredStateByFileName.put(item.getFileName(), desired);
			}
		}
		return pluginManagerService.applyStates(desiredStateByFileName);
	}
}
