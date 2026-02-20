package it4all_plugin_manager.core.model;

import java.util.Objects;

public class PluginRecord {
	private final String fileName;
	private final String realName;
	private PluginState state;

	public PluginRecord(String fileName, String realName, PluginState state) {
		this.fileName = Objects.requireNonNull(fileName, "fileName");
		this.realName = Objects.requireNonNull(realName, "realName");
		this.state = Objects.requireNonNull(state, "state");
	}

	public String getFileName() {
		return fileName;
	}

	public String getRealName() {
		return realName;
	}

	public PluginState getState() {
		return state;
	}

	public void setState(PluginState state) {
		this.state = Objects.requireNonNull(state, "state");
	}

	public PluginRecord copyWithState(PluginState newState) {
		return new PluginRecord(fileName, realName, newState);
	}
}
