package it4all_plugin_manager.core.config;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class EclipsePathResolver {

	public Optional<EclipsePaths> resolve() {
		String homeLocation = System.getProperty("eclipse.home.location");
		if (homeLocation != null && !homeLocation.isBlank()) {
			Path pathFromHome = toPath(homeLocation);
			Path eclipseRoot = findEclipseRoot(pathFromHome);
			if (eclipseRoot != null) {
				return Optional.of(new EclipsePaths(eclipseRoot));
			}
		}

		String userDir = System.getProperty("user.dir");
		if (userDir != null && !userDir.isBlank()) {
			Path eclipseRoot = findEclipseRoot(Paths.get(userDir));
			if (eclipseRoot != null) {
				return Optional.of(new EclipsePaths(eclipseRoot));
			}
		}

		return Optional.empty();
	}

	private Path toPath(String pathValue) {
		try {
			if (pathValue.startsWith("file:")) {
				return Paths.get(URI.create(pathValue));
			}
			return Paths.get(pathValue);
		} catch (Exception exception) {
			return null;
		}
	}

	private Path findEclipseRoot(Path start) {
		if (start == null) {
			return null;
		}

		Path current = start.toAbsolutePath().normalize();
		while (current != null) {
			if (Files.isDirectory(current.resolve("dropins"))) {
				return current;
			}
			current = current.getParent();
		}

		return null;
	}
}
