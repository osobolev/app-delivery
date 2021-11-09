package server.embedded;

import server.core.AppLogger;

import javax.servlet.http.HttpServlet;
import java.io.File;

public interface EmbeddedServer {

    void setLogger(AppLogger logger);

    void setPort(int port);

    interface EmbeddedContext {

        void addServlet(String name, String path, HttpServlet servlet);

        void addStaticServlet(String path);
    }

    EmbeddedContext initContext(String contextPath, File rootDir) throws Exception;

    void start() throws Exception;

    void stop() throws Exception;
}
