package server.jetty;

import apploader.common.AppCommon;
import apploader.common.Application;
import apploader.common.ConfigReader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import server.core.AppLogger;
import server.http.InstallServletBase;
import server.http.ListServletBase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class JettyHttpContainer {

    private final AppLogger mainLogger;

    private final List<AppServerComponent> components = new ArrayList<>();

    private Server jetty;
    private int port;
    private File rootDir;

    public JettyHttpContainer(AppLogger mainLogger) {
        this.mainLogger = mainLogger;
    }

    public void addApplication(AppServerComponent component) {
        components.add(component);
    }

    public void init(int port, File rootDir) throws ServerInitException {
        Log.setLog(new HttpLogger(mainLogger));
        this.jetty = new Server(port);
        this.port = port;
        this.rootDir = rootDir;

        ServletContextHandler ctx = new ServletContextHandler(jetty, "/", ServletContextHandler.NO_SESSIONS);
        try {
            ctx.setBaseResource(Resource.newResource(rootDir.getCanonicalFile().toURI()));
        } catch (IOException ex) {
            throw new ServerInitException(ex);
        }
        List<Application> applications = new ArrayList<>();
        for (AppServerComponent comp : components) {
            String application = comp.application;
            applications.add(new Application(application, comp.getName()));

            AppComponentServlet appServlet = new AppComponentServlet(comp);
            ServletHolder appHolder = new ServletHolder(appServlet);
            appHolder.setName(application);
            ctx.addServlet(appHolder, "/" + application + "/remoting");
        }

        InstallServletBase installServlet = new InstallServlet(mainLogger, rootDir, applications);
        ServletHolder installHolder = new ServletHolder(installServlet);
        installHolder.setName("install");
        ctx.addServlet(installHolder, "/install/*");

        ListServletBase listServlet = new ListServlet(applications);
        ServletHolder listHolder = new ServletHolder(listServlet);
        listHolder.setName("list");
        ctx.addServlet(listHolder, "/" + AppCommon.GLOBAL_APP_LIST);

        ctx.addServlet(new ServletHolder(new DefaultServlet()), "/");
    }

    public void start() throws Exception {
        jetty.start();
        mainLogger.info("Сервер запущен на порту " + port + " в папке " + rootDir.getAbsolutePath());
    }

    public void stop() throws Exception {
        mainLogger.info("Остановка сервера");
        for (AppServerComponent comp : components) {
            comp.shutdown();
        }
        jetty.stop();
        mainLogger.close();
    }

    public int getComponents() {
        return components.size();
    }

    public void error(String message) {
        mainLogger.error(message);
    }

    public void error(Throwable ex) {
        mainLogger.error(ex);
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
