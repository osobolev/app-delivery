package apploader.common;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public final class ConfigReader {

    /**
     * Name of the client config file
     */
    public static final String APPLOADER_PROPERTIES = "apploader.properties";
    /**
     * Name of the URL property in apploader.properties
     */
    public static final String HTTP_SERVER_PROPERTY = "server.url";
    /**
     * Encoding used for app-delivery config files (apploader.properties, install.properties, *_jars.list, proxy.properties)
     */
    public static final String CHARSET = "UTF-8";

    public interface LineWorker {

        boolean workLine(String left, String right);
    }

    public static boolean readConfig(File file, LineWorker worker) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return readConfig(is, worker);
        }
    }

    public static boolean readConfig(InputStream is, LineWorker worker) throws IOException {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is, CHARSET));
        while (true) {
            String line = rdr.readLine();
            if (line == null)
                break;
            line = line.trim();
            if (line.length() <= 0 || line.startsWith("#"))
                continue;
            int p = line.indexOf('=');
            if (p < 0)
                continue;
            String left = line.substring(0, p).trim();
            String right = line.substring(p + 1).trim();
            if (!worker.workLine(left, right))
                return false;
        }
        return true;
    }

    public static void readProperties(Properties props, File file) {
        try {
            if (!file.exists())
                return;
            readConfig(file, (left, right) -> {
                props.put(left, right);
                return true;
            });
        } catch (IOException ex) {
            AppCommon.error(ex);
        }
    }

    public static void readAppProperties(File dir, Properties props) {
        readProperties(props, new File(dir, APPLOADER_PROPERTIES));
    }

    public static URL getServerUrl(Properties properties) throws IOException {
        String serverUrl = properties.getProperty(HTTP_SERVER_PROPERTY);
        if (serverUrl == null)
            return null;
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }
        try {
            return new URL(serverUrl);
        } catch (MalformedURLException ex1) {
            // ignore
        }
        try {
            return new URL("http://" + serverUrl);
        } catch (MalformedURLException ex) {
            throw new IOException("Неправильно задан адрес " + serverUrl, ex);
        }
    }
}
