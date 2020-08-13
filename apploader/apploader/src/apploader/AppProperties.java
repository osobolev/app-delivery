package apploader;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;
import apploader.lib.FileResult;
import apploader.lib.IFileLoader;
import apploader.lib.ILoaderGui;

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
                    if (updateTimeZones(fileResult.updated)) {
                        gui.showWarning("Обновлены данные временных зон, перезапустите приложение");
                        return false;
                    }
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
        return properties;
    }

    private static boolean updateTimeZones(boolean freshUpdater) {
        File file = new File("tzupdater.bat");
        if (file.exists() && !freshUpdater)
            return false;
        try (PrintWriter pw = new PrintWriter(file, AppCommon.BAT_CHARSET)) {
            pw.println("@echo off");
            pw.println("call setjava.bat");
            pw.println("%JAVABIN% -jar tzupdater.jar -u -v");
        } catch (IOException ex) {
            AppCommon.error(ex);
        }
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "tzupdater.bat");
            Process process = pb.start();
            process.waitFor();
        } catch (Exception ex) {
            AppCommon.error(ex);
            file.delete();
        }
        return true;
    }

    private static boolean updateJava(ILoaderGui gui, IFileLoader fileLoader, String right) {
        File jreDir = fileLoader.getLocalFile("jre");
        File javaHome = new File(System.getProperty("java.home")).getAbsoluteFile();
        if (!jreDir.equals(javaHome)) {
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
