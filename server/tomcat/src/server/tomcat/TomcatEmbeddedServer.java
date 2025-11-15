package server.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import server.core.AppLogger;
import server.embedded.EmbeddedServer;
import server.embedded.ServerConfig;

import javax.servlet.http.HttpServlet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class TomcatEmbeddedServer implements EmbeddedServer {

    private final Tomcat tomcat = new Tomcat();

    @Override
    public void setLogger(AppLogger logger) {
        Logger rootLogger = Logger.getLogger("");
        Handler[] existing = rootLogger.getHandlers();
        for (Handler handler : existing) {
            rootLogger.removeHandler(handler);
        }
        rootLogger.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                int level = record.getLevel().intValue();
                String message = record.getMessage();
                if (message != null && level < Level.OFF.intValue()) {
                    if (level >= Level.WARNING.intValue()) {
                        logger.error(message);
                    }
                }
                Throwable error = record.getThrown();
                if (error != null) {
                    logger.error(error);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
                logger.close();
            }
        });
    }

    private void setupServer(ServerConfig config) {
        Service service = tomcat.getService();
        if (config.httpPort != null) {
            Connector http = new Connector();
            http.setPort(config.httpPort.intValue());
            service.addConnector(http);
        }
        if (config.httpsPort != null) {
            Connector https = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            https.setPort(config.httpsPort.intValue());
            https.setSecure(true);
            https.setScheme("https");
            https.setProperty("SSLEnabled", "true");
            https.setProperty("keystoreFile", config.keyStoreFile.getAbsolutePath());
            https.setProperty("keystorePass", config.keyStorePassword);
            service.addConnector(https);
        }
    }

    @Override
    public EmbeddedContext initContext(ServerConfig config) {
        setupServer(config);

        Context ctx = tomcat.addContext("/".equals(config.contextPath) ? "" : config.contextPath, config.rootDir.getAbsolutePath());
        ctx.setSessionTimeout(30);
        Tomcat.addDefaultMimeTypeMappings(ctx);
        ctx.addWelcomeFile("index.html");
        ctx.addWelcomeFile("index.htm");
        return new EmbeddedContext() {

            @Override
            public void addServlet(String name, String path, HttpServlet servlet) {
                Tomcat.addServlet(ctx, name, servlet);
                ctx.addServletMappingDecoded(path, name);
            }

            @Override
            public void addStaticServlet(String path) {
                addServlet("default", path, new DefaultServlet());
            }
        };
    }

    @Override
    public void start() throws Exception {
        tomcat.start();
    }

    @Override
    public void stop() throws Exception {
        tomcat.stop();
    }
}
