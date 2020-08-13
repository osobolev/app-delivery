package apploader.lib;

import apploader.common.ProxyConfig;

import java.net.URL;

public interface ILoaderGui {

    void showStatus(String status);

    void showSuccess(String message);

    void showWarning(String message);

    void showError(String message);

    Result showError2(String message, FileLoader loader);

    Result showWarning2(String message, FileLoader loader);

    Result showWarning3(String message, FileLoader loader);

    void showProxyDialog(ProxyConfig proxy, URL url, FileLoader loader);
}
