package server.install.packers;

import apploader.common.AppCommon;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import server.install.BuildInfo;
import server.install.IOUtils;
import server.install.Packer;
import server.install.PercentCell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

public final class P7zPacker implements Packer {

    private final boolean sfx;
    private final boolean windowsClient;

    public P7zPacker(boolean sfx, boolean windowsClient) {
        this.sfx = sfx;
        this.windowsClient = windowsClient;
    }

    private static final class Compressor {

        private final PercentCell percentCell;
        private final int countFiles;
        private final boolean windowsClient;
        private final SevenZOutputFile output;
        private final int[] count = new int[1];

        Compressor(PercentCell percentCell, int countFiles, boolean windowsClient, SevenZOutputFile output) {
            this.percentCell = percentCell;
            this.countFiles = countFiles;
            this.windowsClient = windowsClient;
            this.output = output;
        }

        @SuppressWarnings("OctalInteger")
        private static int toMask(Set<PosixFilePermission> set) {
            int permissions = 0;

            if (set.contains(PosixFilePermission.OTHERS_READ)) {
                permissions |= 04;
            }
            if (set.contains(PosixFilePermission.OTHERS_WRITE)) {
                permissions |= 02;
            }
            if (set.contains(PosixFilePermission.OTHERS_EXECUTE)) {
                permissions |= 01;
            }

            if (set.contains(PosixFilePermission.GROUP_READ)) {
                permissions |= 040;
            }
            if (set.contains(PosixFilePermission.GROUP_WRITE)) {
                permissions |= 020;
            }
            if (set.contains(PosixFilePermission.GROUP_EXECUTE)) {
                permissions |= 010;
            }

            if (set.contains(PosixFilePermission.OWNER_READ)) {
                permissions |= 0400;
            }
            if (set.contains(PosixFilePermission.OWNER_WRITE)) {
                permissions |= 0200;
            }
            if (set.contains(PosixFilePermission.OWNER_EXECUTE)) {
                permissions |= 0100;
            }

            return permissions;
        }

        private static final byte[] SHEBANG = {'#', '!'};
        private static final byte[] ELF_MAGIC = {0x7F, 'E', 'L', 'F'};

        private static boolean match(byte[] buf, int read, byte[] magic) {
            if (read < magic.length)
                return false;
            for (int i = 0; i < magic.length; i++) {
                if (buf[i] != magic[i])
                    return false;
            }
            return true;
        }

        private static boolean isExecutableFile(Path path) throws IOException {
            if (path.getFileName().toString().toLowerCase().endsWith(".sh"))
                return true;
            byte[] buf = new byte[4];
            int read;
            try (InputStream is = Files.newInputStream(path)) {
                read = is.read(buf);
            }
            return match(buf, read, ELF_MAGIC) || match(buf, read, SHEBANG);
        }

        @SuppressWarnings("OctalInteger")
        private int getWindowsAttributes(Path path, BasicFileAttributes attrs) {
            int windows = 0;
            if (attrs.isDirectory()) {
                windows |= 0x10; // directory flag for Windows
            } else if (attrs.isRegularFile()) {
                windows |= 0x20; // archive flag for Windows
            } else if (attrs.isSymbolicLink()) {
                windows |= 0x400; // link flag for Windows
            }
            if (windowsClient)
                return windows;

            try {
                int unix;
                if (!AppCommon.isWindows()) {
                    PosixFileAttributes posix = Files.readAttributes(path, PosixFileAttributes.class);
                    unix = toMask(posix.permissions());
                } else {
                    if (attrs.isRegularFile()) {
                        if (isExecutableFile(path)) {
                            unix = 0755; // rwxr-xr-x
                        } else {
                            unix = 0644; // rw-r--r--
                        }
                    } else if (attrs.isDirectory()) {
                        unix = 0755; // rwxr-xr-x
                    } else {
                        unix = 0644; // rw-r--r--
                    }
                }
                if ((unix & 0222) == 0) {
                    windows |= 0x1; // readonly flag for Windows
                }
                if (attrs.isSymbolicLink()) {
                    unix |= 0x2000; // symlink flag for UNIX
                }
                // Unix extension flags:
                windows |= 0x8000;
                unix |= 0x8000;
                return (unix << 16) | windows;
            } catch (IOException ex) {
                // ignore
            }
            return windows;
        }

        void compress(File dir, String archivePath) throws IOException {
            File[] files = dir.listFiles();
            if (files == null)
                return;
            for (File file : files) {
                percentCell.workPercent(1, 2, count[0]++, countFiles);
                String nextPath;
                if (archivePath == null) {
                    nextPath = file.getName();
                } else {
                    nextPath = archivePath + "/" + file.getName();
                }
                Path path = file.toPath();
                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                SevenZArchiveEntry entry = output.createArchiveEntry(file, nextPath);
                int windowsAttributes = getWindowsAttributes(path, attrs);
                if (windowsAttributes != 0) {
                    entry.setHasWindowsAttributes(true);
                    entry.setWindowsAttributes(windowsAttributes);
                }
                output.putArchiveEntry(entry);
                if (attrs.isRegularFile()) {
                    try (InputStream is = Files.newInputStream(path)) {
                        output.write(is);
                    }
                } else if (attrs.isSymbolicLink() && !AppCommon.isWindows()) {
                    Path link = Files.readSymbolicLink(path);
                    output.write(link.toString().getBytes(StandardCharsets.UTF_8));
                }
                output.closeArchiveEntry();
                if (attrs.isDirectory()) {
                    compress(file, nextPath);
                }
            }
        }
    }

    @Override
    public String getResultFileName(String baseName) {
        if (!sfx)
            return baseName + ".7z";
        if (windowsClient) {
            return baseName + ".exe";
        } else {
            return baseName + ".sfx";
        }
    }

    private static final class SfxModule {

        final File module;
        final File config;
        final boolean requiresConfig;

        SfxModule(File module, File config, boolean requiresConfig) {
            this.module = module;
            this.config = config;
            this.requiresConfig = requiresConfig;
        }

        boolean isValid() {
            if (module == null)
                return false;
            if (requiresConfig) {
                return config != null;
            } else {
                return true;
            }
        }
    }

    private static File existing(BuildInfo info, String name) {
        if (name == null)
            return null;
        File file = info.getSource(name);
        if (file.isFile())
            return file;
        return null;
    }

    private static SfxModule module(BuildInfo info, String fileName, String configName, boolean requiresConfig) {
        return new SfxModule(
            existing(info, fileName),
            existing(info, configName),
            requiresConfig
        );
    }

    private List<File> getSfxPrepend(BuildInfo info) {
        String sfxProp;
        String configProp;
        if (windowsClient) {
            sfxProp = "7z.sfx.win";
            configProp = "7z.sfx.cfg.win";
        } else {
            sfxProp = "7z.sfx.lin";
            configProp = "7z.sfx.cfg.lin";
        }

        String propModule = info.getProperty(sfxProp, null);
        String propConfig = info.getProperty(configProp, null);
        String simpleModule;
        String configModule;
        String configName;
        if (windowsClient) {
            simpleModule = "7z.win.sfx";
            configModule = "7zS.win.sfx";
            configName = "7z.win.txt";
        } else {
            simpleModule = "7z.lin.sfx";
            configModule = "7zS.lin.sfx";
            configName = "7z.lin.txt";
        }

        List<SfxModule> modules = new ArrayList<>();
        modules.add(module(info, propModule, propConfig, true));
        modules.add(module(info, propModule, configName, false));
        modules.add(module(info, configModule, propConfig, true));
        modules.add(module(info, configModule, configName, true));
        modules.add(module(info, simpleModule, null, false));

        for (SfxModule module : modules) {
            if (module.isValid()) {
                if (module.config != null) {
                    return Arrays.asList(module.module, module.config);
                } else {
                    return Collections.singletonList(module.module);
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean buildResultFile(BuildInfo info, PercentCell percentCell, int countFiles, File result) throws IOException {
        List<File> prepend;
        if (sfx) {
            prepend = getSfxPrepend(info);
            if (prepend.isEmpty()) {
                info.log("No 7z SFX modules found");
                return false;
            }
        } else {
            prepend = Collections.emptyList();
        }
        File target;
        if (prepend.isEmpty()) {
            target = result;
        } else {
            target = new File(result.getParentFile(), result.getName() + ".tmp");
        }
        try (SevenZOutputFile file = new SevenZOutputFile(target)) {
            new Compressor(percentCell, countFiles, windowsClient, file).compress(info.buildDir, null);
        }
        if (!prepend.isEmpty()) {
            try (OutputStream out = Files.newOutputStream(result.toPath())) {
                for (File file : prepend) {
                    IOUtils.copyFile(out, file);
                }
                IOUtils.copyFile(out, target);
            }
            target.delete();
        }
        return true;
    }
}
