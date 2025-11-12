package apploader;

import apploader.client.AppInfo;
import apploader.common.ConfigReader;
import apploader.common.ProxyConfig;
import apploader.lib.HttpInteraction;
import apploader.lib.ILoaderGui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

final class LoaderConfig {

    final URL httpUrl;
    final boolean doNotShow;
    final HttpInteraction http;

    private LoaderConfig(URL httpUrl, boolean doNotShow, HttpInteraction http) {
        this.httpUrl = httpUrl;
        this.doNotShow = doNotShow;
        this.http = http;
    }

    static LoaderConfig load(ILoaderGui gui) {
        Properties apploaderProperties = new Properties();
        File appProperties = ConfigReader.getAppProperties(new File("."));
        ConfigReader.readProperties(apploaderProperties, appProperties, gui::logError);
        String ignoreWarningsProp = "ignore.warnings";
        String ignoreWarnings = System.getProperty(ignoreWarningsProp);
        if (ignoreWarnings == null) {
            ignoreWarnings = apploaderProperties.getProperty(ignoreWarningsProp);
        }
        boolean doNotShow = Boolean.parseBoolean(ignoreWarnings);

        URL httpUrl;
        try {
            httpUrl = ConfigReader.getServerUrl(apploaderProperties);
        } catch (IOException ex) {
            gui.logError(ex);
            gui.showError(ex.getMessage());
            return null;
        }

        ProxyConfig proxy = AppInfo.loadProxy(gui::logError);
        proxy.setLogin();
        HttpInteraction http = new HttpInteraction(proxy);

        if (httpUrl == null) {
            httpUrl = gui.askUrl(http);
            if (httpUrl != null) {
                try {
                    ConfigReader.writeAppProperties(appProperties, httpUrl.toString());
                } catch (Exception ex) {
                    gui.logError(ex);
                }
            }
        }

        return new LoaderConfig(httpUrl, doNotShow, http);
    }
}
