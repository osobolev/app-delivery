package apploader.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AppZip {

    private final File file;
    private final Map<String, UnixZipExtra> extras;

    private AppZip(File file, Map<String, UnixZipExtra> extras) {
        this.file = file;
        this.extras = extras;
    }

    public static AppZip create(File file) {
        if (AppCommon.isWindows()) {
            return new AppZip(file, Collections.emptyMap());
        } else {
            try {
                Map<String, UnixZipExtra> extras = UnixZipExtra.readExtras(file);
                return new AppZip(file, extras);
            } catch (IOException ex) {
                return new AppZip(file, Collections.emptyMap());
            }
        }
    }

    /**
     *  U  G  O
     * rwxrwxrwx
     * 876543210
     */
    @SuppressWarnings("OctalInteger")
    private static Set<PosixFilePermission> fromMask(int mask) {
        Set<PosixFilePermission> perms = EnumSet.noneOf(PosixFilePermission.class);

        if ((mask & 0400) != 0) {
            perms.add(PosixFilePermission.OWNER_READ);
        }
        if ((mask & 0200) != 0) {
            perms.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((mask & 0100) != 0) {
            perms.add(PosixFilePermission.OWNER_EXECUTE);
        }

        if ((mask & 040) != 0) {
            perms.add(PosixFilePermission.GROUP_READ);
        }
        if ((mask & 020) != 0) {
            perms.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((mask & 010) != 0) {
            perms.add(PosixFilePermission.GROUP_EXECUTE);
        }

        if ((mask & 04) != 0) {
            perms.add(PosixFilePermission.OTHERS_READ);
        }
        if ((mask & 02) != 0) {
            perms.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((mask & 01) != 0) {
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
        }

        return perms;
    }

    public void unpackWithExtra(Path destDir, Runnable onEntry) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                UnixZipExtra extra = extras.get(name);
                Path dest = destDir.resolve(name);
                if (extra != null && extra.symLink) {
                    InputStream is = zipFile.getInputStream(entry);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    AppStreamUtils.copyStream(is, bos, -1L);
                    String link = bos.toString("UTF-8");
                    Files.createSymbolicLink(dest, Paths.get(link));
                } else {
                    if (entry.isDirectory()) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(zipFile.getInputStream(entry), dest);
                    }
                }
                if (extra != null) {
                    Set<PosixFilePermission> perms = fromMask(extra.permissions);
                    Files.setPosixFilePermissions(dest, perms);
                }
                if (onEntry != null) {
                    onEntry.run();
                }
            }
        }
    }
}
