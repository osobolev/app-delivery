package server.embedded;

import apploader.common.Application;
import server.http.InstallServletBase;
import sqlg3.remote.server.SQLGLogger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.List;

final class InstallServlet extends InstallServletBase {

    private final SQLGLogger logger;
    private final File rootDir;
    private final List<Application> applications;

    InstallServlet(SQLGLogger logger, File rootDir, List<Application> applications) {
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

    protected SQLGLogger getLogger() {
        return logger;
    }
}
