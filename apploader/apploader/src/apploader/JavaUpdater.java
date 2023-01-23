package apploader;

import apploader.common.AppCommon;
import apploader.common.AppZip;
import apploader.lib.IFileLoader;
import apploader.lib.ILoaderGui;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JavaUpdater {

    private String jre = null;
    private String jre64 = null;

    void add(String left, String right) {
        if ("jre".equalsIgnoreCase(left)) {
            jre = right;
        } else if ("jre64".equalsIgnoreCase(left)) {
            jre64 = right;
        } else if ("jre.win".equalsIgnoreCase(left)) {
            if (AppCommon.isWindows()) {
                jre = right;
            }
        } else if ("jre64.win".equalsIgnoreCase(left)) {
            if (AppCommon.isWindows()) {
                jre64 = right;
            }
        } else if ("jre.lin".equalsIgnoreCase(left)) {
            if (!AppCommon.isWindows()) {
                jre = right;
            }
        } else if ("jre64.lin".equalsIgnoreCase(left)) {
            if (!AppCommon.isWindows()) {
                jre64 = right;
            }
        }
    }

    boolean update(ILoaderGui gui, IFileLoader fileLoader) {
        String javaZip;
        int javaBits;
        if (jre64 != null) {
            if (canRun64Bits()) {
                javaZip = jre64;
                javaBits = 64;
            } else {
                javaZip = jre;
                javaBits = 32;
            }
        } else {
            javaZip = jre;
            javaBits = 0;
        }
        return updateJava(gui, fileLoader, javaZip, javaBits);
    }

    static File getLocalJRE(IFileLoader fileLoader) {
        File javaHome = new File(System.getProperty("java.home")).getAbsoluteFile();
        File jreDir = fileLoader.getLocalFile("jre");
        return jreDir.equals(javaHome) ? javaHome : null;
    }

    private static int currentJavaBits() {
        String model = System.getProperty("sun.arch.data.model");
        if (model != null) {
            if ("64".equals(model)) {
                return 64;
            } else if ("32".equals(model)) {
                return 32;
            }
        }
        String arch = System.getProperty("os.arch");
        if (arch != null) {
            if ("amd64".equals(arch) || "x86_64".equals(arch)) {
                return 64;
            } else if ("x86".equals(arch) || arch.matches("i\\d86")) {
                return 32;
            }
        }
        return 0;
    }

    private static boolean canRun64Bits() {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        if (wow64Arch != null) {
            return wow64Arch.endsWith("64");
        } else if (arch != null) {
            return arch.endsWith("64");
        } else {
            return currentJavaBits() == 64;
        }
    }

    private static boolean sameBitness(int bits1, int bits2) {
        if (bits1 == 0 || bits2 == 0)
            return true;
        return bits1 == bits2;
    }

    private static boolean updateJava(ILoaderGui gui, IFileLoader fileLoader, String newJavaZip, int remoteJavaBits) {
        if (newJavaZip == null)
            return true;
        File javaHome = getLocalJRE(fileLoader);
        if (javaHome == null) {
            // Our java is not in "jre" folder, cannot update it
            return true;
        }
        Pattern pattern = Pattern.compile("java[^-]*-(.*)\\.zip");
        Matcher matcher = pattern.matcher(newJavaZip);
        if (!matcher.matches()) {
            // Java should be zipped
            return true;
        }
        String remoteVersion = matcher.group(1).trim();
        String javaVersion = System.getProperty("java.vm.version");
        if (Objects.equals(remoteVersion, javaVersion) && sameBitness(remoteJavaBits, currentJavaBits())) {
            // We already have this version, skip update
            return true;
        }
        File javaZip = fileLoader.receiveFile(newJavaZip, false).file;
        if (javaZip == null)
            return false;
        try {
            gui.showStatus("Установка новой Java " + remoteVersion + (remoteJavaBits == 0 ? "" : " " + remoteJavaBits + " бита") + "...");
            Path tmpDir = Files.createTempDirectory(Paths.get("."), "jre");
            unzip(javaZip, tmpDir);

            Path newJreDir = Paths.get("jre.new");
            if (Files.exists(newJreDir)) {
                deleteAll(newJreDir);
            }
            Files.move(tmpDir, newJreDir);
            gui.showWarning("Обновлена Java, перезапустите приложение");
            return false;
        } catch (Exception ex) {
            AppCommon.error(ex);
            return false;
        }
    }

    private static void unzip(File file, Path destDir) throws IOException {
        AppZip.create(file).unpackWithExtra(destDir, null);
    }

    private static void deleteAll(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
                    for (Path child : paths) {
                        deleteAll(child);
                    }
                }
            }
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            // ignore
        }
    }
}
