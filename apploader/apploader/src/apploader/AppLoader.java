package apploader;

import apploader.client.AppFactory;
import apploader.client.AppInfo;
import apploader.client.AppRunner;
import apploader.common.AppCommon;
import apploader.common.Application;
import apploader.lib.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.List;

@SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
public final class AppLoader implements AppInfo.AppClassLoader {

    private final ILoaderGui gui;
    private final boolean offline;
    private final IFileLoader fileLoader;
    private final AppClassLoader classLoader;

    AppLoader(ILoaderGui gui, IFileLoader fileLoader, boolean offline) {
        this.gui = gui;
        this.offline = offline;
        this.fileLoader = fileLoader;
        this.classLoader = new AppClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private String updateGlobal(String appToRun) {
        List<Application> allApplications = fileLoader.loadApplications(AppCommon.GLOBAL_APP_LIST, appToRun);
        if (allApplications == null)
            return null;
        AppInfo.applications = allApplications.toArray(new Application[0]);
        StringBuilder buf = new StringBuilder();
        for (Application app : allApplications) {
            if (buf.length() > 0)
                buf.append('\n');
            buf.append(app.id).append(" - ").append(app.name);
            generateBatFile(app.id);
        }
        return buf.toString();
    }

    private static void generateBatFile(String app) {
        if (AppCommon.isWindows()) {
            File batFile = new File(app + "-client.bat");
            if (!batFile.exists()) {
                try {
                    AppCommon.generateBatFile(batFile, app);
                } catch (IOException ex) {
                    AppCommon.error(ex);
                }
            }
        } else {
            File shellFile = new File(app + "-client.sh");
            if (!shellFile.exists()) {
                try {
                    AppCommon.generateShellFile(shellFile, app);
                } catch (IOException ex) {
                    AppCommon.error(ex);
                }
            }
        }
    }

    public AppRunner loadApplication(String application) throws Exception {
        AppProperties properties = AppProperties.updateAppFiles(gui, fileLoader, application);
        gui.showStatus("");
        if (properties == null)
            return null;
        String mainClass = properties.getMainClass();
        if (mainClass == null) {
            gui.showError("Не указан атрибут mainClass");
            return null;
        }
        classLoader.update(properties.jarList, properties.dllList);
        AppInfo.loader = this;
        Class<?> cls = Class.forName(mainClass, true, classLoader);
        if (AppFactory.class.isAssignableFrom(cls)) {
            AppFactory factory = (AppFactory) cls.getDeclaredConstructor().newInstance();
            return factory.newApplication(application);
        } else {
            Method maybeMain;
            try {
                maybeMain = cls.getMethod("main", String[].class);
            } catch (NoSuchMethodException ex) {
                maybeMain = null;
            }
            if (maybeMain == null || !Modifier.isStatic(maybeMain.getModifiers())) {
                gui.showError("Главный класс не является AppFactory и не содержит main");
                return null;
            }
            Method main = maybeMain;
            return args -> {
                try {
                    main.invoke(null, (Object) args);
                } catch (InvocationTargetException ex) {
                    Throwable target = ex.getTargetException();
                    if (target instanceof Exception)
                        throw (Exception) target;
                    throw ex;
                }
            };
        }
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
            AppCommon.error(ex);
            gui.showError(ex.toString());
            return false;
        }
    }

    private static boolean doRun(String[] args) {
        ILoaderGui gui = LoaderGui.create();
        boolean offline = System.getProperty("offline") != null;
        String app = System.getProperty(AppCommon.APPLICATION_PROPERTY);
        IFileLoader fileLoader;
        if (offline) {
            fileLoader = new OfflineFileLoader(gui);
        } else {
            LoaderConfig config = LoaderConfig.load(gui);
            if (config == null)
                return false;
            if ("proxyConfig".equals(app)) {
                gui.showProxyDialog(config.proxy, config.httpUrl, null);
                System.exit(0);
                return true;
            } else if (config.httpUrl == null) {
                gui.showError("Не задан адрес сервера");
                return false;
            }
            fileLoader = new FileLoader(gui, config.httpUrl, null, config.doNotShow, config.proxy);
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
