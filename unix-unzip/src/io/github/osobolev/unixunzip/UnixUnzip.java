package io.github.osobolev.unixunzip;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Unzips ZIP file restoring its entries UNIX permissions.
 * <p>
 * Example:
 * <code><pre>
 * Path file = ...; // ZIP file
 * Path destDir = ...; // Directory to unzip to
 * Map&lt;String, UnixZipExtra&gt; extras = UnixZipExtra.readExtras(file);
 * UnixUnzip.unzip(file, destDir, extras);
 * </pre></code>
 */
public final class UnixUnzip {

    public static void unzip(Path zipFile, Path destDir, Map<String, UnixZipExtra> extras,
                             Consumer<ZipEntry> onEntry) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (onEntry != null) {
                    onEntry.accept(entry);
                }
                UnixZipExtra.restoreEntry(zip, extras, entry, destDir);
            }
        }
    }

    public static void unzip(Path zipFile, Path destDir, Map<String, UnixZipExtra> extras) throws IOException {
        unzip(zipFile, destDir, extras, null);
    }
}
