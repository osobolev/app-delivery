package server.http;

import server.core.AppLogger;
import server.install.ListProfiles;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class ProfileServletBase extends HttpServlet {

    private volatile File root = new File(".");

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        root = getRoot(config);
    }

    protected abstract File getRoot(ServletConfig config);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> profiles = ListProfiles.listProfiles(root, null, getLogger());
        resp.setCharacterEncoding("UTF-8");
        for (String profile : profiles) {
            resp.getWriter().println(profile);
        }
    }

    protected abstract AppLogger getLogger();
}
