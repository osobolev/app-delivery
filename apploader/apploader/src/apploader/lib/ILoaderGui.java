package apploader.lib;

import apploader.common.LogFormatUtil;
import apploader.common.ProxyConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

public interface ILoaderGui {

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    default void logError(Throwable error) {
        try (PrintWriter pw = LogFormatUtil.openRaw("apploader.log")) {
            LogFormatUtil.printStackTrace(pw, error);
        } catch (IOException ex) {
            // ignore
        }
        error.printStackTrace(System.err);
    }

    void showStatus(String status);

    void showSuccess(String message);

    void showWarning(String message);

    void showError(String message);

    Result showError2(String message, FileLoader loader);

    Result showWarning2(String message, FileLoader loader);

    Result showWarning3(String message, FileLoader loader);

    void showProxyDialog(ProxyConfig proxy, URL url, FileLoader loader);

    URL askUrl(ProxyConfig proxy);
}
