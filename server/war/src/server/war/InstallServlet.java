package server.war;

import apploader.common.Application;
import server.http.InstallServletBase;
import txrpc.runtime.TxRpcLogger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.List;

public class InstallServlet extends InstallServletBase {

    protected File getRoot(ServletConfig config) {
        String path = config.getServletContext().getRealPath(".");
        if (path == null) {
            path = ".";
        }
        return new File(path);
    }

    protected List<Application> loadApplications(ServletConfig config) {
        return SingleUtil.getApplications(config);
    }

    protected TxRpcLogger getLogger() {
        return InitListener.getLogger(getServletContext());
    }
}
