package apploader;

import java.io.File;

final class FileResult {

    final File file;
    final boolean isFailCopy;
    final boolean updated;

    FileResult(File file, boolean failCopy, boolean updated) {
        this.file = file;
        this.isFailCopy = failCopy;
        this.updated = updated;
    }
}
