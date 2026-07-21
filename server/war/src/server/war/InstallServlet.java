package server.war;

import apploader.common.Application;
import server.core.AppLogger;
import server.http.InstallServletBase;

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.List;

public class InstallServlet extends InstallServletBase {

    protected File getRoot(ServletConfig config) {
        return SingleUtil.getRoot(config);
    }

    protected List<Application> loadApplications(ServletConfig config) {
        return SingleUtil.getApplications(config);
    }

    protected AppLogger getLogger() {
        return InitListener.getLogger(getServletContext());
    }
}
