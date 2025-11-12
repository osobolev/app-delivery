package apploader.lib;

import apploader.common.ProxyConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public final class HttpUtil {

    public interface HttpHandler<T> {

        T handle(URLConnection conns) throws IOException;
    }

    public static <T> T interact(URL url, ProxyConfig proxy, HttpHandler<T> handler) throws IOException {
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
