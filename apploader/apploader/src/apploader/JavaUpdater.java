package apploader;

import apploader.common.AppCommon;
import apploader.common.AppStreamUtils;
import apploader.common.AppZip;
import apploader.lib.IFileLoader;
import apploader.lib.ILoaderGui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JavaUpdater {

    private static final class JavaZip {

        final String file;
        final boolean optional;

        JavaZip(String file, boolean optional) {
            this.file = file;
            this.optional = optional;
        }
    }

    private JavaZip jre = null;
    private JavaZip jre64 = null;

    private static JavaZip jre(String left, String right) {
        return new JavaZip(right, left.startsWith("?"));
    }

    static boolean match(String left, String mask) {
        return mask.equalsIgnoreCase(left) || ("?" + mask).equalsIgnoreCase(left);
    }

    void add(String left, String right) {
        if (match(left, "jre")) {
            jre = jre(left, right);
        } else if (match(left, "jre64")) {
            jre64 = jre(left, right);
        } else if (match(left, "jre.win")) {
            if (AppCommon.isWindows()) {
                jre = jre(left, right);
            }
        } else if (match(left, "jre64.win")) {
            if (AppCommon.isWindows()) {
                jre64 = jre(left, right);
            }
        } else if (match(left, "jre.lin")) {
            if (!AppCommon.isWindows()) {
                jre = jre(left, right);
            }
        } else if (match(left, "jre64.lin")) {
            if (!AppCommon.isWindows()) {
                jre64 = jre(left, right);
            }
        }
    }

    boolean update(ILoaderGui gui, IFileLoader fileLoader) {
        JavaZip javaZip;
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

    private static boolean updateJava(ILoaderGui gui, IFileLoader fileLoader, JavaZip newJavaZip, int remoteJavaBits) {
        if (newJavaZip == null)
            return true;
        File javaHome = getLocalJRE(fileLoader);
        if (javaHome == null) {
            // Our java is not in "jre" folder, cannot update it
            return true;
        }
        Pattern pattern = Pattern.compile("java[^-]*-(.*)\\.zip");
        Matcher matcher = pattern.matcher(newJavaZip.file);
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
        File javaZip = fileLoader.receiveFile(newJavaZip.file, newJavaZip.optional).file;
        if (javaZip == null)
            return newJavaZip.optional;
        try {
            gui.showStatus("Установка новой Java " + remoteVersion + (remoteJavaBits == 0 ? "" : " " + remoteJavaBits + " бита") + "...");
            Path tmpDir = Files.createTempDirectory(Paths.get("."), "jre");
            unzip(javaZip, tmpDir);

            Path newJreDir = Paths.get("jre.new");
            if (Files.exists(newJreDir)) {
                AppStreamUtils.deleteAll(newJreDir);
            }
            Files.move(tmpDir, newJreDir);
            gui.showWarning("Обновлена Java, перезапустите приложение");
            return false;
        } catch (Exception ex) {
            gui.logError(ex);
            return false;
        }
    }

    private static void unzip(File file, Path destDir) throws IOException {
        AppZip.create(file).unpackWithExtra(destDir, null);
    }
}
