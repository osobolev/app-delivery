package apploader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class HeadResult {

    final long lastModified;
    final int length;
    final boolean needUpdate;

    HeadResult(long lastModified, int length, boolean needUpdate) {
        this.lastModified = lastModified;
        this.length = length;
        this.needUpdate = needUpdate;
    }

    void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        if (length >= 0) {
            int totalRead = 0;
            while (totalRead < length) {
                int rest = length - totalRead;
                int read;
                if (rest > buffer.length) {
                    read = in.read(buffer);
                } else {
                    read = in.read(buffer, 0, rest);
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
                if (read > 0)
                    out.write(buffer, 0, read);
            }
        }
    }
}
