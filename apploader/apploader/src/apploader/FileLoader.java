package apploader;

import apploader.client.Application;
import apploader.client.ProxyConfig;
import apploader.client.SplashStatus;
import apploader.common.ConfigReader;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FileLoader extends IFileLoader {

    private final LoaderGui gui;
    private final URL base;

    private boolean doNotShow;
    private ProxyConfig proxy;
    private boolean connectionProblem = false;

    FileLoader(LoaderGui gui, URL base, boolean doNotShow, ProxyConfig proxy) {
        this.gui = gui;
        this.base = base;
        this.doNotShow = doNotShow;
        this.proxy = proxy;
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
        return block != null && Character.UnicodeBlock.CYRILLIC.equals(block);
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

    private ReceiveResult receiveFileAttempt(File local, String file) throws IOException {
        URL url = new URL(base, file);
        boolean creating = !local.exists();
        HeadResult head = isNeedUpdate(url, local, creating);
        if (head.needUpdate) {
            SplashStatus.setStatus("Обновление " + file + "...");
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

    FileResult receiveFile(String file, boolean silent, boolean noTrace) {
        File local = new File(file).getAbsoluteFile();
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
                        SplashStatus.error(ex);
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

    List<Application> loadApplications(String file, String app) {
        while (true) {
            String errorMessage;
            if (connectionProblem) {
                errorMessage = connectionErrorMessage();
            } else {
                try {
                    return loadApplicationsAttempt(file);
                } catch (IOException ex) {
                    SplashStatus.error(ex);
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
