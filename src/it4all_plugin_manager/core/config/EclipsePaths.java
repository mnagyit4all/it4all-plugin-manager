package it4all_plugin_manager.core.config;

import java.nio.file.Path;
import java.util.Objects;

public class EclipsePaths {
	private final Path eclipseRoot;
	private final Path dropins;
	private final Path temp;

	public EclipsePaths(Path eclipseRoot) {
		this.eclipseRoot = Objects.requireNonNull(eclipseRoot, "eclipseRoot");
		this.dropins = eclipseRoot.resolve("dropins");
		this.temp = dropins.resolve("temp");
	}

	public Path getEclipseRoot() {
		return eclipseRoot;
	}

	public Path getDropins() {
		return dropins;
	}

	public Path getTemp() {
		return temp;
	}
}
