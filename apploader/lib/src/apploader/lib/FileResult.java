package apploader.lib;

import java.io.File;

public final class FileResult {

    public final File file;
    public final boolean isFailCopy;
    public final boolean updated;

    FileResult(File file, boolean failCopy, boolean updated) {
        this.file = file;
        this.isFailCopy = failCopy;
        this.updated = updated;
    }
}
