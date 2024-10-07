package server.embedded;

import apploader.common.Application;
import server.http.InstallServletBase;
import txrpc.remote.server.TxRpcLogger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.List;

final class InstallServlet extends InstallServletBase {

    private final TxRpcLogger logger;
    private final File rootDir;
    private final List<Application> applications;

    InstallServlet(TxRpcLogger logger, File rootDir, List<Application> applications) {
        this.logger = logger;
        this.rootDir = rootDir;
        this.applications = applications;
    }

    protected File getRoot(ServletConfig config) {
        return rootDir;
    }

    protected List<Application> loadApplications(ServletConfig config) {
        return applications;
    }

    protected TxRpcLogger getLogger() {
        return logger;
    }
}
