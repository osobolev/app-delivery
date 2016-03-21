package server.install;

import java.io.*;

public final class IOUtils {

    static void copyStream(OutputStream os, InputStream is) throws IOException {
        try {
            byte[] buffer = new byte[8192];
            while (true) {
                int read = is.read(buffer);
                if (read < 0)
                    break;
                if (read > 0) {
                    os.write(buffer, 0, read);
                }
            }
        } finally {
            close(is);
        }
    }

    public static void copyFile(OutputStream os, File from) throws IOException {
        InputStream is = new FileInputStream(from);
        copyStream(os, is);
    }

    static void copyFile(File to, File from) throws IOException {
        OutputStream os = new FileOutputStream(to);
        try {
            copyFile(os, from);
        } finally {
            close(os);
        }
    }

    static void copyTo(File dirTo, File from) throws IOException {
        File to = new File(dirTo, from.getName());
        copyFile(to, from);
    }

    static void close(Closeable is) {
        try {
            is.close();
        } catch (IOException ex) {
            // ignore
        }
    }
}
