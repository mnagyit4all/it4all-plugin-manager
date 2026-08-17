package pluginmanager.service;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class StorageService {

    private final File storageDir;

    public StorageService(File baseDir) {
        this.storageDir = new File(baseDir, "storage");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    public String copyJarToStorage(File sourceJar) throws IOException {
        File targetFile = new File(storageDir, sourceJar.getName());
        Files.copy(sourceJar.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return "storage/" + sourceJar.getName();
    }

    public File getAbsoluteFile(String relativePath) {
        return new File(storageDir.getParentFile(), relativePath);
    }

    /**
     * Megkeresi a futó Eclipse 'dropins' könyvtárát
     */
    public File getDropinsDir() {
        try {
            URL installUrl = Platform.getInstallLocation().getURL();
            File installDir = new File(FileLocator.toFileURL(installUrl).getFile());
            File dropinsDir = new File(installDir, "dropins");
            if (!dropinsDir.exists()) {
                dropinsDir.mkdirs();
            }
            return dropinsDir;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void syncWithDropins(File sourceJar, boolean enable) throws IOException {
        File dropinsDir = getDropinsDir();
        if (dropinsDir == null || !dropinsDir.exists()) return;

        File targetInDropins = new File(dropinsDir, sourceJar.getName());

        if (enable) {
            // Ha enabled, átmásoljuk a dropins mappába
            Files.copy(sourceJar.toPath(), targetInDropins.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            // Ha disabled, eltávolítjuk a dropins mappából
            if (targetInDropins.exists()) {
                Files.delete(targetInDropins.toPath());
            }
        }
    }
}