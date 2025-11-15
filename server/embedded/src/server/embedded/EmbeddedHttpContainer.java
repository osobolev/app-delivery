package server.embedded;

import apploader.common.AppCommon;
import apploader.common.Application;
import server.core.AppLogger;
import server.http.InstallServletBase;
import server.http.ListServletBase;

import java.util.ArrayList;
import java.util.List;

public final class EmbeddedHttpContainer {

    private final AppLogger mainLogger;
    private final EmbeddedServer server;
    private final List<AppServerComponent> components = new ArrayList<>();

    private String startedWhere = "";

    public EmbeddedHttpContainer(AppLogger mainLogger, EmbeddedServer server) {
        this.mainLogger = mainLogger;
        this.server = server;
    }

    public void addApplication(AppServerComponent component) {
        components.add(component);
    }

    public void init(ServerConfig config) throws ServerInitException {
        server.setLogger(mainLogger);

        EmbeddedServer.EmbeddedContext ctx;
        try {
            ctx = server.initContext(config);
        } catch (Exception ex) {
            throw new ServerInitException(ex);
        }

        List<Application> applications = new ArrayList<>();
        for (AppServerComponent comp : components) {
            String application = comp.application;
            applications.add(new Application(application, comp.getName()));

            AppComponentServlet appServlet = new AppComponentServlet(comp);
            ctx.addServlet(application, "/" + AppCommon.getRemotingContext(application) + "/*", appServlet);
        }

        InstallServletBase installServlet = new InstallServlet(mainLogger, config.rootDir, applications);
        ctx.addServlet("install", "/install/*", installServlet);

        ListServletBase listServlet = new ListServlet(applications);
        ctx.addServlet("list", "/" + AppCommon.GLOBAL_APP_LIST, listServlet);

        ctx.addStaticServlet("/");

        startedWhere = config.toString();
    }

    public void start() throws Exception {
        server.start();
        mainLogger.info("Сервер запущен" + startedWhere);
    }

    public void stop() throws Exception {
        mainLogger.info("Остановка сервера");
        for (AppServerComponent comp : components) {
            comp.shutdown();
        }
        server.stop();
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
}
