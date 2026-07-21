package server.http;

import apploader.common.AppCommon;
import server.core.AppLogger;
import server.install.ListProfiles;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class ProfileServletBase extends HttpServlet {

    private volatile File root = new File(".");

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        root = getRoot(config);
    }

    protected abstract File getRoot(ServletConfig config);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Boolean windowsClient;
        String windows = req.getParameter(AppCommon.PROFILE_WINDOWS);
        if (windows != null) {
            windowsClient = Boolean.parseBoolean(windows);
        } else {
            windowsClient = null;
        }
        int clientBits;
        String bits = req.getParameter(AppCommon.PROFILE_BITS);
        if (bits != null) {
            clientBits = Integer.parseInt(bits);
        } else {
            clientBits = 0;
        }
        Map<String, String> profiles = ListProfiles.listProfiles(root, windowsClient, clientBits, getLogger());
        resp.setCharacterEncoding("UTF-8");
        for (Map.Entry<String, String> entry : profiles.entrySet()) {
            resp.getWriter().println(entry.getKey() + "=" + entry.getValue());
        }
    }

    protected abstract AppLogger getLogger();
}
