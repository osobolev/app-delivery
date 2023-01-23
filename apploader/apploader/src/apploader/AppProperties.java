package apploader;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;
import apploader.lib.FileResult;
import apploader.lib.IFileLoader;
import apploader.lib.ILoaderGui;
import apploader.lib.Result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        JavaUpdater javaUpdater = new JavaUpdater();
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
                String expanded = expand(right);
                if (expanded == null)
                    return true;
                File file = new File(expanded);
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
            } else {
                javaUpdater.add(left, right);
            }
            return true;
        });
        if (!ok)
            return null;
        if (!javaUpdater.update(gui, fileLoader))
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

    private static Boolean updateTimeZones(ILoaderGui gui, IFileLoader fileLoader, boolean newFiles, File dataFile) {
        File successFile = fileLoader.getLocalFile("tzupdater.done");
        if (successFile.exists() && !newFiles)
            return false;
        File javaHome = JavaUpdater.getLocalJRE(fileLoader);
        if (javaHome == null) {
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
}
