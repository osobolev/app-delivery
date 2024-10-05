package apploader.lib;

import java.io.File;

final class HeadResult {

    final long lastModified;
    final long length;

    HeadResult(long lastModified, long length) {
        this.lastModified = lastModified;
        this.length = length;
    }

    static HeadResult fromFile(File file) {
        if (file.exists()) {
            return new HeadResult(file.lastModified(), file.length());
        } else {
            return null;
        }
    }

    boolean isNeedUpdate(HeadResult local) {
        if (local != null) {
            if (this.lastModified > 0) {
                return this.lastModified > local.lastModified;
            } else {
                return this.length != local.length;
            }
        } else {
            return this.length >= 0;
        }
    }
}
