package it4all_plugin_manager.ui;

import it4all_plugin_manager.core.model.PluginRecord;
import it4all_plugin_manager.core.model.PluginState;

public class PluginListItem {
	private final String fileName;
	private final String realName;
	private boolean checked;

	public PluginListItem(String fileName, String realName, boolean checked) {
		this.fileName = fileName;
		this.realName = realName;
		this.checked = checked;
	}

	public static PluginListItem fromRecord(PluginRecord record) {
		return new PluginListItem(record.getFileName(), record.getRealName(), record.getState() == PluginState.DROPINS);
	}

	public String getFileName() {
		return fileName;
	}

	public String getRealName() {
		return realName;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public PluginState toDesiredState() {
		return checked ? PluginState.DROPINS : PluginState.TEMP;
	}
}
