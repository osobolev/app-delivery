package apploader.lib;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;

public final class AppLoaderLib {

    private static final class AppClassLoader extends URLClassLoader {

        private final HashMap<String, File> dllMap = new HashMap<>();

        private AppClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        void addJar(URL url) {
            addURL(url);
        }

        void addDLL(File file) {
            dllMap.put(file.getName().toUpperCase(), file);
        }

        protected String findLibrary(String libname) {
            File file = dllMap.get(libname.toUpperCase());
            if (file == null) {
                file = dllMap.get(System.mapLibraryName(libname).toUpperCase());
            }
            if (file == null)
                return null;
            return file.getAbsolutePath();
        }
    }

    private final ILoaderGui gui;
    private final IFileLoader fileLoader;
    private final AppClassLoader classLoader;
    private boolean updateTimeZones = true;
    private boolean updateCoreJar = true;

    public AppLoaderLib(ILoaderGui gui, IFileLoader fileLoader, URL[] urls, ClassLoader parent) {
        this.gui = gui;
        this.fileLoader = fileLoader;
        this.classLoader = new AppClassLoader(urls, parent);
    }

    public AppProperties updateAppFiles(String application) throws IOException {
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
                if (!updateCoreJar)
                    return true;
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
                if (updateTimeZones && "tzupdater.jar".equals(right)) {
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

    public void updateClassLoader(AppProperties properties) throws Exception {
        List<File> jarList = properties.jarList;
        for (File file : jarList) {
            classLoader.addJar(file.toURI().toURL());
        }
        List<File> dllList = properties.dllList;
        for (File file : dllList) {
            classLoader.addDLL(file);
        }
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
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

    public void setUpdateTimeZones(boolean updateTimeZones) {
        this.updateTimeZones = updateTimeZones;
    }

    public void setUpdateCoreJar(boolean updateCoreJar) {
        this.updateCoreJar = updateCoreJar;
    }
}
