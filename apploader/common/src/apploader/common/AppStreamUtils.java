package apploader.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class AppStreamUtils {

    public static void copyStream(InputStream in, OutputStream out, long length) throws IOException {
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
