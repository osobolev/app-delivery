package apploader.lib;

import apploader.common.AppCommon;
import apploader.common.Application;
import apploader.common.ConfigReader;
import apploader.common.ProxyConfig;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FileLoader extends IFileLoader {

    private final ILoaderGui gui;
    private final URL base;
    private final File root;

    private boolean doNotShow;
    private ProxyConfig proxy;
    private boolean connectionProblem = false;

    public FileLoader(ILoaderGui gui, URL base, File root, boolean doNotShow, ProxyConfig proxy) {
        this.gui = gui;
        this.base = base;
        this.root = root;
        this.doNotShow = doNotShow;
        this.proxy = proxy;
    }

    public void setDoNotShow(boolean doNotShow) {
        this.doNotShow = doNotShow;
    }

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
        proxy.setLogin();
    }

    public URL getUrl() {
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
                    String realResponse = URLDecoder.decode(response, "UTF-8");
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
            try (InputStream in = conn.getInputStream();
                 OutputStream out = new FileOutputStream(to)) {
                head.copyStream(in, out);
            }
            to.setLastModified(head.lastModified);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private ReceiveResult receiveFileAttempt(File local, String file) throws IOException {
        URL url = new URL(base, file);
        boolean creating = !local.exists();
        HeadResult head = isNeedUpdate(url, local, creating);
        if (head.needUpdate) {
            gui.showStatus("Обновление " + file + "...");
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

    public FileResult receiveFile(String file, boolean silent, boolean noTrace) {
        File local = (root == null ? new File(file) : new File(root, file)).getAbsoluteFile();
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
                        AppCommon.error(ex);
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
            try (InputStream in = conn.getInputStream()) {
                List<Application> applications = new ArrayList<>();
                ConfigReader.readConfig(in, (left, right) -> {
                    applications.add(new Application(left, right));
                    return true;
                });
                return applications;
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

    public List<Application> loadApplications(String file, String app) {
        while (true) {
            String errorMessage;
            if (connectionProblem) {
                errorMessage = connectionErrorMessage();
            } else {
                try {
                    return loadApplicationsAttempt(file);
                } catch (IOException ex) {
                    AppCommon.error(ex);
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
}