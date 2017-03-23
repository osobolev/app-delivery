package server.install;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class IOUtils {

    public static void copyFile(OutputStream os, File from) throws IOException {
        Files.copy(from.toPath(), os);
    }

    public static void copyFile(File to, File from) throws IOException {
        Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
