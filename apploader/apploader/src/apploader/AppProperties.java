package apploader;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;
import apploader.lib.FileResult;
import apploader.lib.IFileLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

    static AppProperties updateAppFiles(LoaderGui gui, IFileLoader fileLoader, String application) throws IOException {
        File list = fileLoader.receiveFile(application + "_jars.list", false).file;
        if (list == null)
            return null;
        fileLoader.receiveFile(AppCommon.getSplashName(application), true, true);
        AppProperties properties = new AppProperties();
        boolean ok = ConfigReader.readConfig(list, (left, right) -> {
            boolean jar;
            boolean corejar;
            if ("jar".equalsIgnoreCase(left)) {
                jar = true;
                corejar = false;
            } else if ("corejar".equalsIgnoreCase(left)) {
                jar = true;
                corejar = true;
            } else {
                jar = corejar = false;
            }
            if (jar) {
                FileResult jarResult = fileLoader.receiveFile(right, corejar);
                if (corejar && jarResult.isFailCopy) {
                    gui.showWarning("Обновлен загрузчик приложения, перезапустите его");
                    return false;
                }
                File file = jarResult.file;
                if (file == null)
                    return false;
                properties.jarList.add(file);
            } else if ("mainClass".equalsIgnoreCase(left)) {
                properties.mainClass = right;
            } else if ("dll".equalsIgnoreCase(left)) {
                File file = fileLoader.receiveFile(right, false).file;
                if (file == null)
                    return false;
                properties.dllList.add(file);
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
}
