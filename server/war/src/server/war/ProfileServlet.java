package server.war;

import server.core.AppLogger;
import server.http.ProfileServletBase;

import javax.servlet.ServletConfig;
import java.io.File;

public class ProfileServlet extends ProfileServletBase {

    protected File getRoot(ServletConfig config) {
        return SingleUtil.getRoot(config);
    }

    protected AppLogger getLogger() {
        return InitListener.getLogger(getServletContext());
    }
}
