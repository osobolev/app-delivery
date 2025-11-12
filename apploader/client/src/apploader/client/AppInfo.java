package apploader.client;

import apploader.common.Application;
import apploader.common.ConfigReader;
import apploader.common.ProxyConfig;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.Consumer;

public final class AppInfo {

    public interface AppClassLoader {

        AppRunner loadApplication(String application) throws Exception;
    }

    public static URL httpServerUrl = null;
    public static Application[] applications = null;
    public static AppClassLoader loader = null;
    public static boolean usingApploader = false;

    private static final HashMap<String, IFrame> runningApplications = new HashMap<>();

    public static boolean isRunning(String application) {
        IFrame frame;
        synchronized (runningApplications) {
            frame = runningApplications.get(application);
        }
        if (frame != null) {
            frame.showInFront();
            return true;
        }
        return false;
    }

    public static void addApplication(String application, IFrame frame) {
        synchronized (runningApplications) {
            runningApplications.put(application, frame);
        }
    }

    public static void endApplication(String application) {
        boolean exit;
        synchronized (runningApplications) {
            runningApplications.remove(application);
            exit = runningApplications.isEmpty();
        }
        if (exit) {
            System.exit(0);
        }
    }

    private static final String PROXY_PROPERTIES = "proxy.properties";
    private static final String PTYPE_PROPERTY = "ProxyType";
    private static final String PADDR_PROPERTY = "ProxyAddress";
    private static final String PUSER_PROPERTY = "ProxyLogin";
    private static final String PPASS_PROPERTY = "ProxyPassword";

    public static ProxyConfig loadProxy(Consumer<Throwable> logError) {
        Properties props = new Properties();
        ConfigReader.readProperties(props, new File(PROXY_PROPERTIES), logError);
        String ptype = props.getProperty(PTYPE_PROPERTY);
        String paddr = props.getProperty(PADDR_PROPERTY);
        if (ptype != null && paddr != null) {
            int i = paddr.indexOf(':');
            String host;
            int port = 80;
            if (i < 0) {
                host = paddr;
            } else {
                host = paddr.substring(0, i);
                try {
                    port = Integer.parseInt(paddr.substring(i + 1));
                } catch (NumberFormatException nfex) {
                    // ignore
                }
            }
            Proxy proxy = new Proxy(Proxy.Type.valueOf(ptype), InetSocketAddress.createUnresolved(host, port));
            return new ProxyConfig(proxy, props.getProperty(PUSER_PROPERTY), props.getProperty(PPASS_PROPERTY));
        } else {
            return ProxyConfig.NO_PROXY;
        }
    }

    static void storeProxy(ProxyConfig proxy, Consumer<Throwable> logError) {
        File file = new File(PROXY_PROPERTIES);
        try (PrintWriter pw = new PrintWriter(file, ConfigReader.CHARSET)) {
            if (proxy.proxy.type() != Proxy.Type.DIRECT) {
                pw.println(PTYPE_PROPERTY + "=" + proxy.proxy.type().name());
                pw.println(PADDR_PROPERTY + "=" + proxy.proxy.address().toString());
                pw.println(PUSER_PROPERTY + "=" + proxy.login);
                pw.println(PPASS_PROPERTY + "=" + proxy.password);
            }
        } catch (IOException ex) {
            logError.accept(ex);
        }
    }
}
