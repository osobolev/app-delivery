package apploader;

import apploader.common.ConfigReader;
import apploader.lib.FileResult;
import apploader.lib.IFileLoader;
import apploader.lib.ILoaderGui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static apploader.JavaUpdater.match;

final class AppPropertiesLoader {

    private final ILoaderGui gui;
    private final IFileLoader fileLoader;

    private final List<File> jarList = new ArrayList<>();
    private final List<File> dllList = new ArrayList<>();
    private String mainClass = null;
    private final TZUpdater tzUpdater = new TZUpdater();
    private final JavaUpdater javaUpdater = new JavaUpdater();

    AppPropertiesLoader(ILoaderGui gui, IFileLoader fileLoader) {
        this.gui = gui;
        this.fileLoader = fileLoader;
    }

    private static boolean optional(String left) {
        return left.startsWith("?");
    }

    private static String expand(String str) {
        return Expander.expand(str, property -> {
            String value1 = System.getProperty(property);
            if (value1 != null)
                return value1;
            return System.getenv(property);
        });
    }

    private boolean addCoreJar(String right) {
        FileResult jarResult = fileLoader.receiveFile(right, true, false);
        if (jarResult.isFailCopy || jarResult.updated) {
            gui.showWarning("Обновлен загрузчик приложения, перезапустите приложение");
            return false;
        }
        File file = jarResult.file;
        if (file == null) {
            return false;
        } else {
            jarList.add(file);
            return true;
        }
    }

    private boolean addLib(String left, String right, List<File> addTo) {
        boolean optional = optional(left);
        File file = fileLoader.receiveFile(right, optional).file;
        if (file == null) {
            return optional;
        } else {
            addTo.add(file);
            return true;
        }
    }

    private boolean addLocalLib(String left, String right, List<File> addTo) {
        String expanded = expand(right);
        if (expanded == null)
            return true;
        File file = new File(expanded);
        if (!file.isFile()) {
            boolean optional = optional(left);
            if (optional) {
                return true;
            } else {
                gui.showError("Не найден файл " + file.getAbsolutePath());
                return false;
            }
        } else {
            addTo.add(file);
        }
        return true;
    }

    private boolean addFile(String left, String right) {
        boolean optional = optional(left);
        FileResult fileResult = fileLoader.receiveFile(right, optional);
        File file = fileResult.file;
        if (file == null) {
            return optional;
        } else {
            tzUpdater.add(right, fileResult);
            return true;
        }
    }

    AppProperties load(File list) throws IOException {
        boolean ok = ConfigReader.readConfig(list, (left, right) -> {
            if ("corejar".equalsIgnoreCase(left)) {
                return addCoreJar(right);
            } else if (match(left, "jar")) {
                return addLib(left, right, jarList);
            } else if (match(left, "dll")) {
                return addLib(left, right, dllList);
            } else if (match(left, "localjar")) {
                return addLocalLib(left, right, jarList);
            } else if (match(left, "localdll")) {
                return addLocalLib(left, right, dllList);
            } else if (match(left, "file")) {
                return addFile(left, right);
            } else if ("mainClass".equalsIgnoreCase(left)) {
                mainClass = right;
                return true;
            } else {
                javaUpdater.add(left, right);
                return true;
            }
        });
        if (!ok)
            return null;
        if (!javaUpdater.update(gui, fileLoader))
            return null;
        if (!tzUpdater.update(gui, fileLoader))
            return null;
        return new AppProperties(jarList, dllList, mainClass);
    }
}
