package server.service;

import server.core.AppInit;
import server.core.AppLogger;
import server.embedded.*;
import sqlg3.remote.server.SQLGLogger;
import sqlg3.runtime.SqlTrace;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public final class ServiceServer {

    private final EmbeddedHttpContainer container;

    public ServiceServer(AppLogger mainLogger, EmbeddedServer server) {
        this.container = new EmbeddedHttpContainer(mainLogger, server);
    }

    public ServiceServer(EmbeddedServer server) {
        this(new EmbeddedLogger(), server);
    }

    public void showError(String message) {
        container.error(message);
    }

    public void showError(Throwable ex) {
        container.error(ex);
    }

    public boolean runServer(Integer port, String context, File rootDir, AppLogin login, Function<SQLGLogger, SqlTrace> trace, List<AppServerComponent> comps) throws Exception {
        for (AppServerComponent component : comps) {
            boolean inited = false;
            try {
                AppInit appInit = component.getInit();
                AppLogger logger = appInit.createLogger();
                component.init(login, logger, trace.apply(logger));
                container.addApplication(component);
                inited = true;
            } finally {
                if (!inited) {
                    component.shutdown();
                }
            }
        }
        if (container.getComponents() <= 0) {
            container.error("Нет ни одного серверного компонента");
            return false;
        } else {
            container.init(port, context, rootDir);
            container.start();
            for (AppServerComponent component : comps) {
                component.start();
            }
            return true;
        }
    }

    public void stop() throws Exception {
        container.stop();
    }
}
