package apploader;

import apploader.client.AppFactory;
import apploader.client.AppInfo;
import apploader.client.AppRunner;
import apploader.client.Application;
import apploader.common.ClientBat;
import apploader.common.ConfigReader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({
    "ResultOfMethodCallIgnored", "AssignmentToStaticFieldFromInstanceMethod",
    "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr"
})
public final class AppLoader implements AppInfo.AppClassLoader {

    private static final String GLOBAL_APP_LIST = "global_app.list";

    private static final String APPLICATION_PROPERTY = "application";

    private static final class AppProperties {

        private final List<File> jarList = new ArrayList<File>();
        private final List<File> dllList = new ArrayList<File>();
        private String mainClass = null;
    }

    private static final class AppClassLoader extends URLClassLoader {

        private final HashMap<String, File> dllMap = new HashMap<String, File>();

        private AppClassLoader() {
            super(new URL[0]);
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

    private final LoaderGui gui;
    private final boolean offline;
    private final IFileLoader fileLoader;
    private final AppClassLoader classLoader = new AppClassLoader();

    AppLoader(LoaderGui gui, IFileLoader fileLoader, boolean offline) {
        this.gui = gui;
        this.offline = offline;
        this.fileLoader = fileLoader;
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private String updateGlobal(String appToRun) {
        List<Application> allApplications = fileLoader.loadApplications(GLOBAL_APP_LIST, appToRun);
        if (allApplications == null)
            return null;
        AppInfo.applications = allApplications.toArray(new Application[allApplications.size()]);
        StringBuilder buf = new StringBuilder();
        for (Application app : allApplications) {
            if (buf.length() > 0)
                buf.append('\n');
            buf.append(app.id).append(" - ").append(app.name);
            generateBatFile(app.id);
        }
        return buf.toString();
    }

    private AppProperties updateAll(String application) throws IOException {
        File list = fileLoader.receiveFile(application + "_jars.list", false, false).file;
        if (list == null)
            return null;
        fileLoader.receiveFile(ClientBat.getSplashName(application), true, true);
        final AppProperties properties = new AppProperties();
        boolean ok = ConfigReader.readConfig(list, new ConfigReader.LineWorker() {
            public boolean workLine(String left, String right) {
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
                    FileResult jarResult = fileLoader.receiveFile(right, corejar, false);
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
                    File file = fileLoader.receiveFile(right, false, false).file;
                    if (file == null)
                        return false;
                    properties.dllList.add(file);
                } else if ("file".equalsIgnoreCase(left)) {
                    FileResult fileResult = fileLoader.receiveFile(right, false, false);
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
            }
        });
        if (!ok)
            return null;
        return properties;
    }

    private Class<?> loadClass(AppProperties properties) throws Exception {
        if (properties.mainClass == null) {
            gui.showError("Не указан атрибут mainClass");
            return null;
        }
        AppInfo.loader = this;
        List<File> jarList = properties.jarList;
        for (File file : jarList) {
            classLoader.addJar(file.toURI().toURL());
        }
        List<File> dllList = properties.dllList;
        for (File file : dllList) {
            classLoader.addDLL(file);
        }
        return Class.forName(properties.mainClass, true, classLoader);
    }

    private static void generateBatFile(String app) {
        File file = new File(app + "-client.bat");
        if (file.exists())
            return;
        try {
            ClientBat.generateBatFile(file, app);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean updateTimeZones(boolean freshUpdater) {
        File file = new File("tzupdater.bat");
        if (file.exists() && !freshUpdater)
            return false;
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.println("@echo off");
            pw.println("call setjava.bat");
            pw.println("%JAVABIN% -jar tzupdater.jar -u -v");
            pw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "tzupdater.bat");
        try {
            Process process = pb.start();
            process.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
            file.delete();
        }
        return true;
    }

    public static void setStatus(String status) {
        if (status.length() > 0) {
            System.out.println(status);
        }
        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null)
            return;
        Graphics2D g = splash.createGraphics();
        if (g == null)
            return;
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int height = fm.getHeight();
        int y0 = splash.getSize().height - 10 - height;
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, y0, splash.getSize().width, height);
        g.setPaintMode();
        g.setColor(Color.black);
        int y1 = y0 + fm.getAscent();
        g.drawString(status, 10, y1);
        splash.update();
    }

    public AppRunner loadApplication(String application) throws Exception {
        AppProperties properties = updateAll(application);
        setStatus("");
        if (properties == null)
            return null;
        Class<?> cls = loadClass(properties);
        if (cls == null)
            return null;
        AppFactory factory = (AppFactory) cls.newInstance();
        return factory.newApplication(application);
    }

    private boolean run(String app, String[] args) {
        if (!offline) {
            String available = updateGlobal(app);
            if (available == null) {
                gui.showError("Нет доступа к приложениям на сервере");
                return false;
            }
            if (app == null) {
                if (available.length() > 0) {
                    gui.showError("Не указано приложение для запуска.\nДоступные приложения:\n" + available);
                } else {
                    gui.showError("Не указано приложение для запуска");
                }
                return false;
            }
            if ("install".equalsIgnoreCase(app)) {
                if (available.length() <= 0) {
                    gui.showError("Нет доступных приложений");
                    return false;
                } else {
                    if (args.length > 0) {
                        String arg = args[0].trim();
                        String message = arg.length() <= 0 || "-".equals(arg) ? "Установка программы завершена!" : arg;
                        gui.showSuccess(message);
                    }
                    System.exit(0);
                    return true;
                }
            }
        }
        try {
            AppRunner runner = loadApplication(app);
            if (runner != null) {
                runner.runGui(args);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            gui.showError(ex.toString());
            return false;
        }
    }

    private static boolean doRun(String[] args) {
        LoaderGui gui = new LoaderGui();
        boolean offline = System.getProperty("offline") != null;
        String app = System.getProperty(APPLICATION_PROPERTY);
        IFileLoader fileLoader;
        if (offline) {
            fileLoader = new OfflineFileLoader(gui);
        } else {
            LoaderConfig config = LoaderConfig.load(gui);
            if (config == null)
                return false;
            if ("proxyConfig".equals(app)) {
                gui.showProxyDialog(null, config.proxy, config.httpUrl, null);
                System.exit(0);
                return true;
            } else if (config.httpUrl == null) {
                gui.showError("Не задан адрес сервера");
                return false;
            }
            fileLoader = new FileLoader(gui, config.httpUrl, config.doNotShow, config.proxy);
            AppInfo.httpServerUrl = config.httpUrl;
        }
        AppLoader loader = new AppLoader(gui, fileLoader, offline);
        return loader.run(app, args);
    }

    public static void main(String[] args) {
        AppInfo.usingApploader = true;
        if (!doRun(args)) {
            System.exit(2);
        }
    }
}
