package server.embedded;

import apploader.common.Application;
import server.core.AppLogger;
import server.http.InstallServletBase;

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.List;

final class InstallServlet extends InstallServletBase {

    private final AppLogger logger;
    private final File rootDir;
    private final List<Application> applications;

    InstallServlet(AppLogger logger, File rootDir, List<Application> applications) {
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

    protected AppLogger getLogger() {
        return logger;
    }
}
