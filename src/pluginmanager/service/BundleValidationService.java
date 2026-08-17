package pluginmanager.service;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class BundleValidationService {

    public static class ValidationResult {
        private final boolean valid;
        private final String symbolicName;
        private final String version;
        private final String errorMessage;

        public ValidationResult(boolean valid, String symbolicName, String version, String errorMessage) {
            this.valid = valid;
            this.symbolicName = symbolicName;
            this.version = version;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() { return valid; }
        public String getSymbolicName() { return symbolicName; }
        public String getVersion() { return version; }
        public String getErrorMessage() { return errorMessage; }
    }

    public ValidationResult validateBundle(File jarFile) {
        if (jarFile == null || !jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
            return new ValidationResult(false, null, null, "A megadott fájl nem létezik vagy nem .jar kiterjesztésű.");
        }

        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return new ValidationResult(false, null, null, "A JAR fájl nem tartalmaz MANIFEST.MF fájlt.");
            }

            Attributes mainAttrs = manifest.getMainAttributes();
            String symbolicName = mainAttrs.getValue("Bundle-SymbolicName");
            String version = mainAttrs.getValue("Bundle-Version");

            if (symbolicName == null || symbolicName.trim().isEmpty()) {
                return new ValidationResult(false, null, null, "Hiányzó 'Bundle-SymbolicName' a MANIFEST.MF-ből. Nem érvényes OSGi bundle.");
            }

            // Tisztítás: a pl. "com.example.plugin;singleton:=true" formátumból kinyerjük csak az azonosítót
            if (symbolicName.contains(";")) {
                symbolicName = symbolicName.split(";")[0].trim();
            }

            if (version == null || version.trim().isEmpty()) {
                version = "0.0.0";
            }

            return new ValidationResult(true, symbolicName, version, null);

        } catch (IOException e) {
            return new ValidationResult(false, null, null, "Hiba a JAR fájl olvasásakor: " + e.getMessage());
        }
    }
}