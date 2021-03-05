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

    public void init(Integer maybePort, String maybeContext, File rootDir) throws ServerInitException {
        Log.setLog(new HttpLogger(mainLogger));
        this.rootDir = rootDir;

        PortAndContext pc = getPortAndContext(maybePort, maybeContext);
        this.port = pc.port;
        this.jetty = new Server(port);

        ServletContextHandler ctx = new ServletContextHandler(jetty, pc.context, ServletContextHandler.NO_SESSIONS);
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

    private static final class PortAndContext {

        final int port;
        final String context;

        PortAndContext(int port, String context) {
            this.port = port;
            this.context = context;
        }
    }

    private static int getPort(URL serverUrl, Integer maybePort) {
        if (maybePort != null)
            return maybePort.intValue();
        if (serverUrl != null) {
            int port = serverUrl.getPort();
            if (port > 0) {
                return port;
            }
        }
        return 80;
    }

    private static String getContext(URL serverUrl, String maybeContext) {
        if (maybeContext != null)
            return maybeContext;
        if (serverUrl != null) {
            String path = serverUrl.getPath();
            if (path != null && !path.isEmpty()) {
                if (path.length() > 1 && path.endsWith("/")) {
                    return path.substring(0, path.length() - 1);
                } else {
                    return path;
                }
            }
        }
        return "/";
    }

    private PortAndContext getPortAndContext(Integer maybePort, String maybeContext) {
        if (maybePort != null && maybeContext != null)
            return new PortAndContext(maybePort.intValue(), maybeContext);
        Properties props = new Properties();
        ConfigReader.readAppProperties(rootDir, props);
        URL serverUrl = null;
        try {
            serverUrl = ConfigReader.getServerUrl(props);
        } catch (IOException ex) {
            error(ex);
        }
        int port = getPort(serverUrl, maybePort);
        String context = getContext(serverUrl, maybeContext);
        return new PortAndContext(port, context);
    }
}
