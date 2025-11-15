package server.jetty;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import server.core.AppLogger;
import server.embedded.EmbeddedServer;
import server.embedded.ServerConfig;

import javax.servlet.http.HttpServlet;

public final class JettyEmbeddedServer implements EmbeddedServer {

    private final boolean sessions;
    private final Server jetty = new Server();

    public JettyEmbeddedServer(boolean sessions) {
        this.sessions = sessions;
    }

    public JettyEmbeddedServer() {
        this(false);
    }

    @Override
    public void setLogger(AppLogger logger) {
        Log.setLog(new HttpLogger(logger));
    }

    private void setupServer(ServerConfig config) {
        if (config.httpPort != null) {
            ServerConnector http = new ServerConnector(jetty, new HttpConnectionFactory());
            http.setPort(config.httpPort.intValue());
            jetty.addConnector(http);
        }
        if (config.httpsPort != null) {
            SslContextFactory ssl = new SslContextFactory.Server();
            ssl.setKeyStorePath(config.keyStoreFile.getAbsolutePath());
            ssl.setKeyStorePassword(Password.obfuscate(config.keyStorePassword));

            HttpConfiguration httpsConfig = new HttpConfiguration();
            httpsConfig.addCustomizer(new SecureRequestCustomizer());

            ServerConnector https = new ServerConnector(jetty, ssl, new HttpConnectionFactory(httpsConfig));
            https.setPort(config.httpsPort.intValue());
            jetty.addConnector(https);
        }
    }

    @Override
    public EmbeddedContext initContext(ServerConfig config) throws Exception {
        setupServer(config);

        int options = sessions ? ServletContextHandler.SESSIONS : ServletContextHandler.NO_SESSIONS;
        ServletContextHandler ctx = new ServletContextHandler(jetty, config.contextPath, options);
        ctx.setBaseResource(Resource.newResource(config.rootDir.getCanonicalFile().toURI()));
        return new EmbeddedContext() {

            @Override
            public void addServlet(String name, String path, HttpServlet servlet) {
                ServletHolder holder = new ServletHolder(servlet);
                holder.setName(name);
                ctx.addServlet(holder, path);
            }

            @Override
            public void addStaticServlet(String path) {
                ctx.addServlet(new ServletHolder(new DefaultServlet()), path);
            }
        };
    }

    @Override
    public void start() throws Exception {
        jetty.start();
    }

    @Override
    public void stop() throws Exception {
        jetty.stop();
    }
}
