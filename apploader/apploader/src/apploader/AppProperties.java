package apploader;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;
import apploader.lib.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class AppProperties {

    final List<File> jarList = new ArrayList<>();
    final List<File> dllList = new ArrayList<>();
    private String mainClass = null;

    private AppProperties() {
    }

    String getMainClass() {
        return mainClass;
    }

    private static String expand(String str) {
        return Expander.expand(str, property -> {
            String value1 = System.getProperty(property);
            if (value1 != null)
                return value1;
            return System.getenv(property);
        });
    }

    static AppProperties updateAppFiles(ILoaderGui gui, IFileLoader fileLoader, String application) throws IOException {
        File list = fileLoader.receiveFile(application + "_jars.list", false).file;
        if (list == null)
            return null;
        fileLoader.receiveFile(AppCommon.getSplashName(application), true, true);
        AppProperties properties = new AppProperties();
        FileResult[] newTZUpdater = new FileResult[1];
        FileResult[] newTimeZones = new FileResult[1];
        boolean ok = ConfigReader.readConfig(list, (left, right) -> {
            boolean jar;
            boolean corejar;
            boolean localjar;
            if ("jar".equalsIgnoreCase(left)) {
                jar = true;
                corejar = false;
                localjar = false;
            } else if ("corejar".equalsIgnoreCase(left)) {
                jar = true;
                corejar = true;
                localjar = false;
            } else {
                jar = corejar = false;
                localjar = "localjar".equalsIgnoreCase(left);
            }
            if (jar) {
                FileResult jarResult = fileLoader.receiveFile(right, corejar);
                if (corejar && jarResult.isFailCopy) {
                    gui.showWarning("Обновлен загрузчик приложения, перезапустите приложение");
                    return false;
                }
                File file = jarResult.file;
                if (file == null)
                    return false;
                properties.jarList.add(file);
            } else if ("dll".equalsIgnoreCase(left)) {
                File file = fileLoader.receiveFile(right, false).file;
                if (file == null)
                    return false;
                properties.dllList.add(file);
            } else if (localjar || "localdll".equalsIgnoreCase(left)) {
                File file = new File(expand(right));
                if (!file.isFile()) {
                    gui.showError("Не найден файл " + file.getAbsolutePath());
                    return false;
                }
                if (localjar) {
                    properties.jarList.add(file);
                } else {
                    properties.dllList.add(file);
                }
            } else if ("file".equalsIgnoreCase(left)) {
                FileResult fileResult = fileLoader.receiveFile(right, false);
                File file = fileResult.file;
                if (file == null)
                    return false;
                if ("tzupdater.jar".equals(right)) {
                    newTZUpdater[0] = fileResult;
                } else if ("tzdata.tar.gz".equals(right)) {
                    newTimeZones[0] = fileResult;
                }
            } else if ("?file".equalsIgnoreCase(left)) {
                fileLoader.receiveFile(right, true, true);
            } else if ("mainClass".equalsIgnoreCase(left)) {
                properties.mainClass = right;
            } else if ("jre".equalsIgnoreCase(left)) {
                return updateJava(gui, fileLoader, right);
            }
            return true;
        });
        if (!ok)
            return null;
        if (newTZUpdater[0] != null && newTimeZones[0] != null) {
            boolean anyUpdated = newTZUpdater[0].updated || newTimeZones[0].updated;
            Boolean updateResult = updateTimeZones(gui, fileLoader, anyUpdated, newTimeZones[0].file);
            if (updateResult == null)
                return null;
            if (updateResult.booleanValue()) {
                gui.showWarning("Обновлены данные часовых поясов, перезапустите приложение");
                return null;
            }
        }
        return properties;
    }

    private static File getJavaHome() {
        return new File(System.getProperty("java.home")).getAbsoluteFile();
    }

    private static boolean isLocalJRE(IFileLoader fileLoader, File javaHome) {
        File jreDir = fileLoader.getLocalFile("jre");
        return jreDir.equals(javaHome);
    }

    private static Boolean updateTimeZones(ILoaderGui gui, IFileLoader fileLoader, boolean newFiles, File dataFile) {
        File successFile = fileLoader.getLocalFile("tzupdater.done");
        if (successFile.exists() && !newFiles)
            return false;
        File javaHome = getJavaHome();
        if (!isLocalJRE(fileLoader, javaHome)) {
            // Our java is not in "jre" folder, cannot update it
            return false;
        }
        boolean success = false;
        try {
            File javaBin = new File(new File(javaHome, "bin"), AppCommon.isWindows() ? "java.exe" : "java");
            ProcessBuilder pb = new ProcessBuilder(
                javaBin.getAbsolutePath(), "-jar", "tzupdater.jar", "-v", "-f", "-l", dataFile.toURI().toString()
            );
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(fileLoader.getLocalFile("tzupdater.log")));
            Process process = pb.start();
            int exitCode = process.waitFor();
            boolean ok;
            if (exitCode == 0) {
                ok = true;
            } else {
                Result ans = gui.showWarning3("Ошибка обновления часовых поясов", null);
                if (ans == Result.ABORT)
                    return null;
                ok = ans == Result.IGNORE;
            }
            if (ok) {
                successFile.createNewFile();
            }
            success = ok;
        } catch (Exception ex) {
            AppCommon.error(ex);
        }
        if (!success) {
            successFile.delete();
        }
        return true;
    }

    private static boolean updateJava(ILoaderGui gui, IFileLoader fileLoader, String right) {
        File javaHome = getJavaHome();
        if (!isLocalJRE(fileLoader, javaHome)) {
            // Our java is not in "jre" folder, cannot update it
            return true;
        }
        if (!(right.startsWith("java") && right.endsWith(".zip"))) {
            // Java should be zipped
            return true;
        }
        String remoteVersion = right.substring(4, right.length() - 4).trim();
        String javaVersion = System.getProperty("java.vm.version");
        if (remoteVersion.equals(javaVersion)) {
            // We already have this version, skip update
            return true;
        }
        File javaZip = fileLoader.receiveFile(right, false).file;
        if (javaZip == null)
            return false;
        try {
            gui.showStatus("Установка новой Java...");
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
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path dest = destDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(zipFile.getInputStream(entry), dest);
                }
            }
        }
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
