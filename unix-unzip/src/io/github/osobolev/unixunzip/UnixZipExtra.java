package io.github.osobolev.unixunzip;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads/restores UNIX permissions of ZIP file entries.
 * <p>
 * Example:
 * <code><pre>
 * Path file = ...; // ZIP file
 * Path destDir = ...; // Directory to unzip to
 * Map&lt;String, UnixZipExtra&gt; extras = UnixZipExtra.readExtras(file);
 * try (ZipFile zipFile = new ZipFile(file.toFile())) {
 *     Enumeration&lt;? extends ZipEntry&gt; entries = zipFile.entries();
 *     while (entries.hasMoreElements()) {
 *         ZipEntry entry = entries.nextElement();
 *         UnixZipExtra.restoreEntry(zipFile, extras, entry, destDir);
 *     }
 * }
 * </pre></code>
 *
 * @see <a href="https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT">ZIP format specification</a>
 * @see <a href="https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html">ZIP structure with diagrams and examples</a>
 */
public final class UnixZipExtra {

    public final boolean symLink;
    public final int permissions;

    public UnixZipExtra(boolean symLink, int permissions) {
        this.symLink = symLink;
        this.permissions = permissions;
    }

    private static int scan(byte[] haystack, int haystackTo, byte[] needle) {
        int i = haystackTo - needle.length;
        while (i >= 0) {
            boolean match = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    match = false;
                    break;
                }
            }
            if (match)
                return i;
            i--;
        }
        return -1;
    }

    private static byte[] concat(byte[] a1, int from1, int to1, byte[] a2) {
        int len1 = to1 - from1;
        byte[] sum = new byte[len1 + (a2 == null ? 0 : a2.length)];
        System.arraycopy(a1, from1, sum, 0, len1);
        if (a2 != null) {
            System.arraycopy(a2, 0, sum, len1, a2.length);
        }
        return sum;
    }

    private static int get16(byte[] buf, int ofs) {
        int b0 = buf[ofs] & 0xFF;
        int b1 = buf[ofs + 1] & 0xFF;
        return (b1 << 8) | b0;
    }

    private static long get32(byte[] buf, int ofs) {
        long b0 = buf[ofs] & 0xFF;
        long b1 = buf[ofs + 1] & 0xFF;
        long b2 = buf[ofs + 2] & 0xFF;
        long b3 = buf[ofs + 3] & 0xFF;
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

    private static final class Location {

        final long offset;
        final long size;

        Location(long offset, long size) {
            this.offset = offset;
            this.size = size;
        }
    }

    private static Location checkSanity(byte[] buf, int from, long diskStart, long fileSize) {
        int commentLen = get16(buf, from + 0x14);
        if (commentLen < 0)
            return null;
        long cdSize = get32(buf, from + 0x0c);
        if (cdSize <= 0)
            return null;
        long cdOfs = get32(buf, from + 0x10);
        if (cdOfs < 0 || cdOfs >= diskStart || cdOfs + cdSize > diskStart)
            return null;
        long end = diskStart + 0x16 + commentLen;
        if (end != fileSize)
            return null;
        return new Location(cdOfs, cdSize);
    }

    @SuppressWarnings("OctalInteger")
    private static int parseEntry(RandomAccessFile file, byte[] header, Map<String, UnixZipExtra> extra) throws IOException {
        file.readFully(header);
        int hostSystem = (get16(header, 0x04) >> 8) & 0xFF;
        int nameLen = get16(header, 0x1c);
        int extraLen = get16(header, 0x1e);
        int commLen = get16(header, 0x20);
        // get permissions for UNIX only:
        if (hostSystem == 0x03) {
            // For "external" field structure for UNIX see diagram at the bottom of https://unix.stackexchange.com/a/14727:
            long external = get32(header, 0x26);

            byte[] nameBytes = new byte[nameLen];
            file.readFully(nameBytes);
            file.skipBytes(extraLen + commLen);

            int fileType = (int) ((external >> 28) & 0xF);
            boolean symLink = fileType == 012;

            int permissions = (int) ((external >> 16) & 0777);

            String name = new String(nameBytes, StandardCharsets.UTF_8);
            extra.put(name, new UnixZipExtra(symLink, permissions));
        } else {
            file.skipBytes(nameLen + extraLen + commLen);
        }
        return header.length + nameLen + extraLen + commLen;
    }

    private static Location findCentralDirectory(RandomAccessFile file) throws IOException {
        byte[] piece = new byte[256];
        byte[] buf = null;
        long fileSize = file.length();
        long pos = fileSize;
        byte[] signature = {0x50, 0x4b, 0x05, 0x06};
        while (pos > 0) {
            pos -= piece.length;
            if (pos < 0) {
                pos = 0;
            }
            file.seek(pos);
            int read = file.read(piece);
            buf = concat(piece, 0, read, buf);
            int found = scan(buf, Math.min(read + signature.length - 1, buf.length), signature);
            if (found >= 0) {
                Location location = checkSanity(buf, found, pos + found, fileSize);
                if (location != null)
                    return location;
            }
            // <End of central directory record> size + max comment size:
            if (buf.length >= 0x16 + 0xFFFF)
                break;
        }
        return null;
    }

    /**
     * Reads UNIX permissions of ZIP file entries.
     *
     * @return the map of ZIP entry names to their UNIX permissions
     */
    public static Map<String, UnixZipExtra> readExtras(Path path) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
            Location location = findCentralDirectory(file);
            if (location == null)
                return Collections.emptyMap();
            file.seek(location.offset);
            byte[] header = new byte[0x2e];
            long parsed = 0;
            Map<String, UnixZipExtra> extra = new HashMap<>();
            while (parsed < location.size) {
                parsed += parseEntry(file, header, extra);
            }
            return extra;
        }
    }

    /**
     * Converts integer UNIX permissions mask to set of {@link PosixFilePermission}
     */
    @SuppressWarnings("OctalInteger")
    public static Set<PosixFilePermission> fromMask(int mask) {
        //  U  G  O
        // rwxrwxrwx
        // 876543210
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

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[256];
        while (true) {
            int read = in.read(buffer);
            if (read < 0)
                break;
            out.write(buffer, 0, read);
        }
    }

    public interface GetEntryContent {

        InputStream getInputStream(ZipEntry entry) throws IOException;
    }

    /**
     * Restores an entry from the ZIP file restoring its UNIX permissions (if any).
     * Can be used for {@link ZipFile} and {@link java.util.zip.ZipInputStream}.
     *
     * @param extras the map of ZIP entry names to their UNIX permissions (can be read from the ZIP file by {@link #readExtras(Path)}).
     * @param entry ZIP entry to restore
     * @param getContent returns ZIP entry data
     * @param destDir where to put the restored file/directory
     */
    public static void restoreEntry(Map<String, UnixZipExtra> extras,
                                    ZipEntry entry, GetEntryContent getContent, Path destDir) throws IOException {
        String name = entry.getName();
        UnixZipExtra extra = extras.get(name);
        Path dest = destDir.resolve(name);
        if (extra != null && extra.symLink) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            copyStream(getContent.getInputStream(entry), bos);
            String link = bos.toString("UTF-8");
            Files.createDirectories(dest.getParent());
            Files.createSymbolicLink(dest, Paths.get(link));
        } else {
            if (entry.isDirectory()) {
                Files.createDirectories(dest);
            } else {
                Files.createDirectories(dest.getParent());
                Files.copy(getContent.getInputStream(entry), dest);
            }
        }
        if (extra != null) {
            Set<PosixFilePermission> perms = fromMask(extra.permissions);
            Files.setPosixFilePermissions(dest, perms);
        }
    }

    /**
     * Restores an entry from the ZIP file restoring its UNIX permissions (if any).
     *
     * @param zipFile ZIP file
     * @param extras the map of ZIP entry names to their UNIX permissions (can be read from the ZIP file by {@link #readExtras(Path)}).
     * @param entry ZIP entry to restore
     * @param destDir where to put the restored file/directory
     */
    public static void restoreEntry(ZipFile zipFile, Map<String, UnixZipExtra> extras,
                                    ZipEntry entry, Path destDir) throws IOException {
        restoreEntry(extras, entry, zipFile::getInputStream, destDir);
    }
}
