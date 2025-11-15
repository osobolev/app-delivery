package server.embedded;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ServerConfig {

    public final String contextPath;
    public final File rootDir;
    public final Integer httpPort;
    public final Integer httpsPort;
    public final File keyStoreFile;
    public final String keyStorePassword;

    public ServerConfig(String contextPath, File rootDir, Integer httpPort, Integer httpsPort, File keyStoreFile, String keyStorePassword) {
        this.contextPath = contextPath;
        this.rootDir = rootDir;
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.keyStoreFile = keyStoreFile;
        this.keyStorePassword = keyStorePassword;
    }

    @Override
    public String toString() {
        List<String> ports = new ArrayList<>(2);
        if (httpPort != null) {
            ports.add("HTTP=" + httpPort);
        }
        if (httpsPort != null) {
            ports.add("HTTPS=" + httpsPort);
        }
        return " на порту " + String.join(", ", ports) + " в папке " + rootDir.getAbsolutePath();
    }
}
