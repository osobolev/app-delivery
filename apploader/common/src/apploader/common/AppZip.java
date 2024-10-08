package apploader.common;

import io.github.osobolev.unixunzip.UnixUnzip;
import io.github.osobolev.unixunzip.UnixZipExtra;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public final class AppZip {

    private final Path file;
    private final Map<String, UnixZipExtra> extras;

    private AppZip(Path file, Map<String, UnixZipExtra> extras) {
        this.file = file;
        this.extras = extras;
    }

    public static AppZip create(File file) {
        Path path = file.toPath();
        Map<String, UnixZipExtra> extras = Collections.emptyMap();
        if (!AppCommon.isWindows()) {
            try {
                extras = UnixZipExtra.readExtras(path);
            } catch (IOException ex) {
                // ignore
            }
        }
        return new AppZip(path, extras);
    }

    public void unpackWithExtra(Path destDir, Runnable onEntry) throws IOException {
        UnixUnzip.unzip(file, destDir, extras, entry -> onEntry.run());
    }
}
