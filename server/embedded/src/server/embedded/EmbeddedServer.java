package server.embedded;

import server.core.AppLogger;

import javax.servlet.http.HttpServlet;

public interface EmbeddedServer {

    void setLogger(AppLogger logger);

    interface EmbeddedContext {

        void addServlet(String name, String path, HttpServlet servlet);

        void addStaticServlet(String path);
    }

    EmbeddedContext initContext(ServerConfig config) throws Exception;

    void start() throws Exception;

    void stop() throws Exception;
}
