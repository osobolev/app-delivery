package apploader.lib;

import apploader.common.ProxyConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public final class HttpInteraction {

    public interface HttpHandler<T> {

        T handle(URLConnection conns) throws IOException;
    }

    private ProxyConfig proxy;

    public HttpInteraction(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public <T> T interact(URL url, HttpHandler<T> handler) throws IOException {
        URLConnection conn = url.openConnection(proxy.proxy);
        try {
            return handler.handle(conn);
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }
}
