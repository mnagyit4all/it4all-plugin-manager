package it4all_plugin_manager.core.config;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class EclipsePathResolver {

	public Optional<EclipsePaths> resolve() {
		String homeLocation = System.getProperty("eclipse.home.location");
		if (homeLocation != null && !homeLocation.isBlank()) {
			Path pathFromHome = toPath(homeLocation);
			if (pathFromHome != null) {
				return Optional.of(new EclipsePaths(pathFromHome));
			}
		}

		String userDir = System.getProperty("user.dir");
		if (userDir != null && !userDir.isBlank()) {
			return Optional.of(new EclipsePaths(Paths.get(userDir)));
		}

		return Optional.empty();
	}

	private Path toPath(String pathValue) {
		if (pathValue.startsWith("file:")) {
			return Paths.get(URI.create(pathValue));
		}
		return Paths.get(pathValue);
	}
}
