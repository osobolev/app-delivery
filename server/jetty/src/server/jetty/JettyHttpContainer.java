package server.jetty;

import apploader.common.ConfigReader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.FileResource;
import server.http.Application;
import server.http.InstallServletBase;
import server.http.ListServletBase;
import sqlg2.db.SimpleLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class JettyHttpContainer {

    private final List<AppServerComponent> components = new ArrayList<AppServerComponent>();

    private final SimpleLogger logger = new SimpleLogger("appserver.log");

    private Server jetty;
    private int port;

    public void addApplication(AppServerComponent component) {
        components.add(component);
    }

    public void init(int port, File rootDir) throws ServerInitException {
        Log.setLog(new HttpLogger(logger));
        this.jetty = new Server(port);
        this.port = port;

        ServletContextHandler ctx = new ServletContextHandler(jetty, "/", ServletContextHandler.NO_SESSIONS);
        try {
            ctx.setBaseResource(FileResource.newResource(rootDir.getCanonicalFile().toURI().toURL()));
        } catch (IOException ex) {
            throw new ServerInitException(ex);
        }
        List<Application> applications = new ArrayList<Application>();
        for (AppServerComponent comp : components) {
            String application = comp.application;
            applications.add(new Application(application, comp.getName()));

            AppComponentServlet appServlet = new AppComponentServlet(comp);
            ServletHolder appHolder = new ServletHolder(appServlet);
            appHolder.setName(application);
            ctx.addServlet(appHolder, "/" + application + "/remoting");
        }

        InstallServletBase installServlet = new InstallServlet(logger, rootDir, applications);
        ServletHolder installHolder = new ServletHolder(installServlet);
        installHolder.setName("install");
        ctx.addServlet(installHolder, "/install/*");

        ListServletBase listServlet = new ListServlet(applications);
        ServletHolder listHolder = new ServletHolder(listServlet);
        listHolder.setName("list");
        ctx.addServlet(listHolder, "/global_app.list");

        ctx.addServlet(new ServletHolder(new DefaultServlet()), "/");
    }

    public void start() throws Exception {
        jetty.start();
        logger.info("Сервер запущен на порту " + port);
    }

    public void stop() throws Exception {
        logger.info("Остановка сервера");
        for (AppServerComponent comp : components) {
            comp.shutdown();
        }
        jetty.stop();
    }

    public int getComponents() {
        return components.size();
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(Throwable ex) {
        logger.error(ex);
    }

    public int getDefaultPort(File dir) {
        Properties props = new Properties();
        ConfigReader.readAppProperties(dir, props);
        URL serverUrl = null;
        try {
            serverUrl = ConfigReader.getServerUrl(props);
        } catch (IOException ex) {
            error(ex);
        }
        if (serverUrl != null && serverUrl.getPort() > 0) {
            return serverUrl.getPort();
        }
        return 80;
    }
}
