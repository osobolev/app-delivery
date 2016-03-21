package server.tomcat;

import server.http.Application;
import server.http.InstallServletBase;

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.List;

public final class InstallServlet extends InstallServletBase {

    protected File getRoot(ServletConfig config) {
        String path = config.getServletContext().getRealPath(".");
        if (path == null) {
            path = ".";
        }
        return new File(path);
    }

    protected List<Application> loadApplications(ServletConfig config) {
        return SingleUtil.getApplication(config);
    }

    protected void error(String message) {
        log(message);
    }

    protected void error(Throwable error) {
        log(error.toString(), error);
    }
}
