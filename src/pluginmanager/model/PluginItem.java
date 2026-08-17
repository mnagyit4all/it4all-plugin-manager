package pluginmanager.model;

import java.util.Objects;

public class PluginItem {
    private String symbolicName;
    private String version;
    private String jarPath;
    private boolean enabled;

    public PluginItem() {}

    public PluginItem(String symbolicName, String version, String jarPath, boolean enabled) {
        this.symbolicName = symbolicName;
        this.version = version;
        this.jarPath = jarPath;
        this.enabled = enabled;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginItem that = (PluginItem) o;
        return Objects.equals(symbolicName, that.symbolicName) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbolicName, version);
    }
}