package it4all_plugin_manager.core.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PluginFileMover {

	public boolean move(Path source, Path target, CollisionPolicy policy) throws IOException {
		if (!Files.exists(source)) {
			return false;
		}

		Files.createDirectories(target.getParent());

		if (Files.exists(target) && policy == CollisionPolicy.SKIP) {
			return false;
		}

		if (Files.exists(target) && policy == CollisionPolicy.OVERWRITE) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			return true;
		}

		Files.move(source, target);
		return true;
	}
}
