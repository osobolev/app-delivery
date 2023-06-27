package server.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import server.core.AppLogger;
import server.embedded.EmbeddedServer;

import javax.servlet.http.HttpServlet;
import java.io.File;

public final class JettyEmbeddedServer implements EmbeddedServer {

    private final boolean sessions;
    private Server jetty;

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

    @Override
    public void setPort(int port) {
        jetty = new Server(port);
    }

    @Override
    public EmbeddedContext initContext(String contextPath, File rootDir) throws Exception {
        int options = sessions ? ServletContextHandler.SESSIONS : ServletContextHandler.NO_SESSIONS;
        ServletContextHandler ctx = new ServletContextHandler(jetty, contextPath, options);
        ctx.setBaseResource(Resource.newResource(rootDir.getCanonicalFile().toURI()));
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
