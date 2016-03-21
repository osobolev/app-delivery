package apploader.common;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public final class ConfigReader {

    public static final String APPLOADER_PROPERTIES = "apploader.properties";
    public static final String HTTP_SERVER_PROPERTY = "server.url";
    public static final String CHARSET = "Cp1251";

    public interface LineWorker {

        boolean workLine(String left, String right);
    }

    public static void close(Closeable rdr) {
        if (rdr != null) {
            try {
                rdr.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    public static boolean readConfig(File file, LineWorker worker) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            return readConfig(is, worker);
        } finally {
            close(is);
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

    public static void readProperties(final Properties props, File file) {
        try {
            if (!file.exists())
                return;
            readConfig(file, new LineWorker() {
                public boolean workLine(String left, String right) {
                    props.put(left, right);
                    return true;
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
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
