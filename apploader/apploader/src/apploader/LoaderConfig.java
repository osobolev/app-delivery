package apploader;

import apploader.client.AppInfo;
import apploader.common.AppCommon;
import apploader.common.ConfigReader;
import apploader.common.ProxyConfig;
import apploader.lib.ILoaderGui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

final class LoaderConfig {

    final URL httpUrl;
    final boolean doNotShow;
    final ProxyConfig proxy;

    private LoaderConfig(URL httpUrl, boolean doNotShow, ProxyConfig proxy) {
        this.httpUrl = httpUrl;
        this.doNotShow = doNotShow;
        this.proxy = proxy;
    }

    static LoaderConfig load(ILoaderGui gui) {
        Properties apploaderProperties = new Properties();
        File appProperties = ConfigReader.getAppProperties(new File("."));
        ConfigReader.readProperties(apploaderProperties, appProperties);
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
            AppCommon.error(ex);
            gui.showError(ex.getMessage());
            return null;
        }

        ProxyConfig proxy = AppInfo.loadProxy();
        proxy.setLogin();

        return new LoaderConfig(httpUrl, doNotShow, proxy);
    }
}
