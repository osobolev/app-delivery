package apploader;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;
import apploader.lib.FileResult;
import apploader.lib.IFileLoader;
import apploader.lib.ILoaderGui;

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
        TZUpdater tzUpdater = new TZUpdater();
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
                tzUpdater.add(right, fileResult);
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
        if (!tzUpdater.update(gui, fileLoader))
            return null;
        return properties;
    }
}
