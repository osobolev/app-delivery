package apploader;

import apploader.client.*;
import apploader.common.ConfigReader;

import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
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
    private final URL base;
    private final boolean offline;

    private boolean doNotShow;
    private ProxyConfig proxy;
    private boolean connectionProblem = false;

    private final AppClassLoader classLoader = new AppClassLoader();

    AppLoader(LoaderGui gui, URL base, boolean doNotShow, ProxyConfig proxy, boolean offline) {
        this.gui = gui;
        this.base = base;
        this.doNotShow = doNotShow;
        this.proxy = proxy;
        this.offline = offline;
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private String updateGlobal() {
        List<Application> allApplications = loadApplications(GLOBAL_APP_LIST);
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

    private static String getSplashName(String application) {
        return application + "_splash.jpg";
    }

    private AppProperties updateAll(String application) throws IOException {
        File list = receiveFile(application + "_jars.list", false).file;
        if (list == null)
            return null;
        receiveFile(getSplashName(application), true, true);
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
                    FileResult jarResult = receiveFile(right, corejar);
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
                    File file = receiveFile(right, false).file;
                    if (file == null)
                        return false;
                    properties.dllList.add(file);
                } else if ("file".equalsIgnoreCase(left)) {
                    FileResult fileResult = receiveFile(right, false);
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
                    receiveFile(right, true, true);
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
            PrintWriter pw = new PrintWriter(file);
            pw.println("@echo off");
            pw.println("call checknew.bat");
            pw.println("call setjava.bat");
            String splash = " -splash:" + getSplashName(app);
            pw.println("%JAVABIN% -D" + APPLICATION_PROPERTY + "=" + app + splash + " -jar apploader.jar %*");
            pw.close();
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

    void setDoNotShow(boolean doNotShow) {
        this.doNotShow = doNotShow;
    }

    ProxyConfig getProxy() {
        return proxy;
    }

    void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
        proxy.setLogin();
    }

    URL getUrl() {
        return base;
    }

    private String connectionErrorMessage() {
        return "Ошибка соединения с " + base;
    }

    private String translate(IOException ex) {
        if (ex instanceof ConnectException) {
            return connectionErrorMessage();
        } else {
            return ex.getMessage();
        }
    }

    private static boolean isGood(char ch) {
        if (ch >= ' ' && ch < 127)
            return true;
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        if (block != null && Character.UnicodeBlock.CYRILLIC.equals(block))
            return true;
        return false;
    }

    private static int countGood(String str) {
        int good = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (isGood(ch)) {
                good++;
            }
        }
        return good;
    }

    private HeadResult isNeedUpdate(URL url, File local, boolean creating) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection(proxy.proxy);
            conn.setRequestMethod("HEAD");
            conn.connect();
            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                long lastModified = conn.getLastModified();
                int length = conn.getContentLength();
                boolean needUpdate;
                if (!creating) {
                    if (lastModified > 0) {
                        needUpdate = lastModified > local.lastModified();
                    } else {
                        needUpdate = length != local.length();
                    }
                } else {
                    needUpdate = length >= 0;
                }
                return new HeadResult(lastModified, length, needUpdate);
            } else {
                String response = conn.getResponseMessage();
                if (response != null) {
                    char[] chars = response.toCharArray();
                    byte[] bytes = new byte[chars.length];
                    for (int i = 0; i < chars.length; i++)
                        bytes[i] = (byte) chars[i];
                    String response2 = new String(bytes);
                    String realResponse;
                    if (countGood(response) > countGood(response2)) {
                        realResponse = response;
                    } else {
                        realResponse = response2;
                    }
                    throw new IOException(code + ": " + realResponse);
                } else {
                    throw new IOException("Ошибка " + code);
                }
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void transferFile(URL url, File to, HeadResult head) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection(proxy.proxy);
            conn.setRequestMethod("GET");
            conn.connect();
            to.delete();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = conn.getInputStream();
                out = new FileOutputStream(to);
                head.copyStream(in, out);
            } finally {
                ConfigReader.close(in);
                ConfigReader.close(out);
            }
            to.setLastModified(head.lastModified);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
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

    private ReceiveResult receiveFileAttempt(File local, String file) throws IOException {
        URL url = new URL(base, file);
        boolean creating = !local.exists();
        HeadResult head = isNeedUpdate(url, local, creating);
        if (head.needUpdate) {
            setStatus("Обновление " + file + "...");
            File parent = local.getAbsoluteFile().getParentFile();
            parent.mkdirs();
            File neo = new File(parent, local.getName() + ".tmp");
            boolean ok = false;
            IOException lastError = null;
            for (int i = 0; i < 3; i++) {
                try {
                    transferFile(url, neo, head);
                    ok = true;
                    break;
                } catch (IOException ex) {
                    lastError = ex;
                }
            }
            if (!ok) {
                throw lastError;
            } else {
                if (!creating) {
                    local.delete();
                }
                if (!neo.renameTo(local)) {
                    File neoCopy = new File(parent, local.getName() + ".new");
                    neoCopy.delete();
                    neo.renameTo(neoCopy);
                    return ReceiveResult.UPDATE_FAIL;
                }
            }
            return ReceiveResult.OK_UPDATE;
        } else {
            return ReceiveResult.OK_CACHED;
        }
    }

    private FileResult receiveFile(String file, boolean silent, boolean noTrace) {
        File local = new File(file).getAbsoluteFile();
        if (offline) {
            if (local.exists()) {
                return new FileResult(local, false, false);
            } else {
                if (!silent) {
                    gui.showError("Файл " + file + " отсутствует");
                }
                return new FileResult(null, false, false);
            }
        }
        while (true) {
            String errorMessage;
            if (connectionProblem) {
                errorMessage = connectionErrorMessage();
            } else {
                try {
                    ReceiveResult rr = receiveFileAttempt(local, file);
                    if (rr == ReceiveResult.UPDATE_FAIL) {
                        Result ans = showFileError(local, silent, file, "Ошибка перезаписи новой версии файла", false);
                        return new FileResult(ans == Result.IGNORE ? local : null, true, false);
                    } else {
                        return new FileResult(local, false, rr == ReceiveResult.OK_UPDATE);
                    }
                } catch (IOException ex) {
                    if (!noTrace) {
                        ex.printStackTrace();
                        if (ex instanceof ConnectException) {
                            connectionProblem = true;
                        }
                    }
                    errorMessage = translate(ex);
                }
            }
            Result ans = showFileError(local, silent, file, errorMessage, true);
            switch (ans) {
            case IGNORE: return new FileResult(local, false, false);
            case ABORT: return new FileResult(null, false, false);
            }
            connectionProblem = false;
        }
    }

    private FileResult receiveFile(String file, boolean silent) {
        return receiveFile(file, silent, false);
    }

    private Result showFileError(File local, boolean silent, String file, String error, boolean allowRetry) {
        if (local != null && local.isFile()) {
            if (silent || doNotShow)
                return Result.IGNORE;
            String message = "Ошибка обновления файла " + file + ":\n" + error + "\nПродолжить?";
            if (allowRetry) {
                return gui.showWarning3(message, this);
            } else {
                return gui.showWarning2(message, this);
            }
        } else {
            if (!silent) {
                String message = "Ошибка обновления файла " + file + ":\n" + error;
                if (allowRetry) {
                    return gui.showError2(message, this);
                } else {
                    gui.showError(message);
                }
            }
            return Result.ABORT;
        }
    }

    private List<Application> transferApplications(URL url) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection(proxy.proxy);
            conn.setRequestMethod("GET");
            conn.connect();
            InputStream in = null;
            try {
                in = conn.getInputStream();
                final List<Application> applications = new ArrayList<Application>();
                ConfigReader.readConfig(
                    in,
                    new ConfigReader.LineWorker() {
                        public boolean workLine(String left, String right) {
                            applications.add(new Application(left, right));
                            return true;
                        }
                    }
                );
                return applications;
            } finally {
                ConfigReader.close(in);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private List<Application> loadApplicationsAttempt(String file) throws IOException {
        URL url = new URL(base, file);
        IOException lastError = null;
        for (int i = 0; i < 3; i++) {
            try {
                return transferApplications(url);
            } catch (IOException ex) {
                lastError = ex;
            }
        }
        throw lastError;
    }

    private List<Application> loadApplications(String file) {
        while (true) {
            String errorMessage;
            if (connectionProblem) {
                errorMessage = connectionErrorMessage();
            } else {
                try {
                    return loadApplicationsAttempt(file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    if (ex instanceof ConnectException) {
                        connectionProblem = true;
                    }
                    errorMessage = translate(ex);
                }
            }
            Result ans = showAppError(file, errorMessage);
            switch (ans) {
            case IGNORE: return Collections.emptyList();
            case ABORT: return null;
            }
            connectionProblem = false;
        }
    }

    private Result showAppError(String file, String error) {
        String message = "Ошибка обновления файла " + file + ":\n" + error;
        return gui.showError2(message, this);
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
            String available = updateGlobal();
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
                runner.runGui(args, null);
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
        LoaderConfig config;
        if (offline) {
            config = LoaderConfig.offline();
        } else {
            config = LoaderConfig.load(gui);
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
        }
        AppInfo.httpServerUrl = config.httpUrl;
        AppLoader loader = new AppLoader(gui, config.httpUrl, config.doNotShow, config.proxy, offline);
        return loader.run(app, args);
    }

    public static void main(String[] args) {
        AppInfo.usingApploader = true;
        if (!doRun(args)) {
            System.exit(2);
        }
    }
}
