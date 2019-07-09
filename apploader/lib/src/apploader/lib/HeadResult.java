package apploader.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        if (length >= 0) {
            long totalRead = 0;
            while (true) {
                long rest = length - totalRead;
                if (rest <= 0)
                    break;
                int read;
                if (rest > buffer.length) {
                    read = in.read(buffer);
                } else {
                    read = in.read(buffer, 0, (int) rest);
                }
                if (read < 0)
                    break;
                out.write(buffer, 0, read);
                totalRead += read;
            }
        } else {
            while (true) {
                int read = in.read(buffer);
                if (read < 0)
                    break;
                out.write(buffer, 0, read);
            }
        }
    }
}
