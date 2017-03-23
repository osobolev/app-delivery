package server.service;

import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;
import server.core.AppInit;
import server.jetty.AppLogin;
import server.jetty.AppServerComponent;
import server.jetty.JettyHttpContainer;
import server.jetty.ServerInitException;
import sqlg2.db.SQLGLogger;
import sqlg2.db.SqlTrace;

import java.io.File;
import java.util.List;

public abstract class ServerServiceBase extends AbstractService {

    protected final JettyHttpContainer container = new JettyHttpContainer();

    public int serviceMain(String[] strings) {
        try {
            File rootDir = getRoot();
            int port = getPort(rootDir);
            AppLogin login = getLogin();
            SqlTrace trace = getTrace();
            List<AppServerComponent> components = getComponents();
            for (AppServerComponent component : components) {
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
                System.exit(3);
            } else {
                container.init(port, rootDir);
                container.start();
                for (AppServerComponent component : components) {
                    component.start();
                }
            }
        } catch (Throwable ex) {
            container.error(ex);
            System.exit(2);
        }
        return 0;
    }

    protected SqlTrace getTrace() {
        return SqlTrace.DEFAULT_TRACE;
    }

    protected abstract AppLogin getLogin();

    protected int getPort(File rootDir) throws ServerInitException {
        return container.getDefaultPort(rootDir);
    }

    protected File getRoot() throws ServerInitException {
        return new File(".");
    }

    protected abstract List<AppServerComponent> getComponents() throws ServerInitException;

    public int serviceRequest(int control) throws ServiceException {
        if (control == SERVICE_CONTROL_STOP || control == SERVICE_CONTROL_SHUTDOWN) {
            if (container != null) {
                try {
                    container.stop();
                } catch (Exception ex) {
                    throw new ServiceException(ex);
                }
            }
        }
        return super.serviceRequest(control);
    }
}
