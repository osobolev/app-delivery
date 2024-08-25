package apploader.common;

import io.github.osobolev.unixzip.UnixZipExtra;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AppZip {

    private final File file;
    private final Map<String, UnixZipExtra> extras;

    private AppZip(File file, Map<String, UnixZipExtra> extras) {
        this.file = file;
        this.extras = extras;
    }

    public static AppZip create(File file) {
        Map<String, UnixZipExtra> extras = Collections.emptyMap();
        if (!AppCommon.isWindows()) {
            try {
                extras = UnixZipExtra.readExtras(file);
            } catch (IOException ex) {
                // ignore
            }
        }
        return new AppZip(file, extras);
    }

    public void unpackWithExtra(Path destDir, Runnable onEntry) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (onEntry != null) {
                    onEntry.run();
                }
                UnixZipExtra.restoreEntry(zipFile, extras, entry, destDir);
            }
        }
    }
}
