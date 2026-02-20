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

		try {
			if (Files.exists(target) && policy == CollisionPolicy.OVERWRITE) {
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}

			Files.move(source, target);
			return true;
		} catch (IOException moveException) {
			return copyDeleteFallback(source, target, policy, moveException);
		}
	}

	private boolean copyDeleteFallback(Path source, Path target, CollisionPolicy policy, IOException moveException)
		throws IOException {
		try {
			if (Files.exists(target) && policy == CollisionPolicy.SKIP) {
				return false;
			}

			if (Files.exists(target) && policy == CollisionPolicy.OVERWRITE) {
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.copy(source, target);
			}
			Files.delete(source);
			return true;
		} catch (IOException fallbackException) {
			fallbackException.addSuppressed(moveException);
			throw new IOException(
				"Plugin move failed from '" + source + "' to '" + target + "'. "
					+ "Possible reason: file is locked by running Eclipse/plugin.",
				fallbackException);
		}
	}
}
