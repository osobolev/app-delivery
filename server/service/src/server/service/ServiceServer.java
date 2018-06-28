package server.service;

import server.core.AppInit;
import server.jetty.AppLogin;
import server.jetty.AppServerComponent;
import server.jetty.JettyHttpContainer;
import sqlg2.db.SQLGLogger;
import sqlg2.db.SqlTrace;

import java.io.File;
import java.util.List;

public final class ServiceServer {

    private final JettyHttpContainer container = new JettyHttpContainer();

    public void showError(String message) {
        container.error(message);
    }

    public void showError(Throwable ex) {
        container.error(ex);
    }

    public boolean runServer(Integer port, File rootDir, AppLogin login, SqlTrace trace, List<AppServerComponent> comps) throws Exception {
        for (AppServerComponent component : comps) {
            boolean inited = false;
            try {
                AppInit appInit = component.getInit();
                SQLGLogger logger = appInit.createLogger();
                component.init(login, logger, trace);
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
            int realPort = port == null ? container.getDefaultPort(rootDir) : port.intValue();
            container.init(realPort, rootDir);
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
