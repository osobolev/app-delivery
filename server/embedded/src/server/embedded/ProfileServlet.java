package server.embedded;

import server.core.AppLogger;
import server.http.ProfileServletBase;

import javax.servlet.ServletConfig;
import java.io.File;

final class ProfileServlet extends ProfileServletBase {

    private final AppLogger logger;
    private final File rootDir;

    ProfileServlet(AppLogger logger, File rootDir) {
        this.logger = logger;
        this.rootDir = rootDir;
    }

    protected File getRoot(ServletConfig config) {
        return rootDir;
    }

    protected AppLogger getLogger() {
        return logger;
    }
}
