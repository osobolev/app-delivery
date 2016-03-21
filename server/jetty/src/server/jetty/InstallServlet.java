package server.jetty;

import server.http.Application;
import server.http.InstallServletBase;
import sqlg2.db.SimpleLogger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.List;

final class InstallServlet extends InstallServletBase {

    private final SimpleLogger logger;
    private final File rootDir;
    private final List<Application> applications;

    InstallServlet(SimpleLogger logger, File rootDir, List<Application> applications) {
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

    protected void error(String message) {
        logger.error(message);
    }

    protected void error(Throwable error) {
        logger.error(error);
    }
}
