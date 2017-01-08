package server.http;

import server.install.BuildException;
import server.install.IOUtils;
import server.install.InstallBuilder;
import server.install.SourceFiles;
import sqlg2.db.SQLGLogger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InstallServletBase extends AppServletBase {

    private volatile File root = new File(".");
    private final Map<String, InstallState> map = new HashMap<String, InstallState>();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        root = getRoot(config);
    }

    protected abstract File getRoot(ServletConfig config);

    private synchronized InstallState getBuilder(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String profile = pathInfo == null || pathInfo.isEmpty() ? "" : pathInfo.substring(1);
        InstallState state = map.get(profile);

        if (state == null) {
            List<String> apps = new ArrayList<String>();
            List<Application> applications = getApplications();
            for (Application application : applications) {
                apps.add(application.id);
            }
            String fullUrl = req.getRequestURL().toString();
            String path = req.getServletPath();
            String url;
            if (fullUrl.endsWith(path)) {
                url = fullUrl.substring(0, fullUrl.length() - path.length()) + "/";
            } else {
                url = req.getLocalName() + ":" + req.getLocalPort();
            }
            SourceFiles src = new SourceFiles(root, apps, profile, url);
            state = new InstallState(new InstallBuilder(src));
            map.put(profile, state);
        }
        return state;
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String initStr = req.getParameter("init");
        InstallState state = getBuilder(req);
        boolean init = initStr != null && !"0".equals(initStr);
        state.createInstaller(init, this);
        resp.setStatus(HttpServletResponse.SC_OK);
        boolean done;
        boolean ok;
        if (state.creating) {
            done = ok = false;
        } else {
            done = true;
            ok = state.builder.getReadyInstaller() != null;
        }
        String json = "{'percent':'" + state.builder.getPercentCell().get() + "%', 'done':" + done + ", 'ok':" + ok + ", 'error':'" + state.error.replace('\'', '"') + "'}";
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println(json);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            InstallState state = getBuilder(req);
            File installer = state.builder.getInstaller();
            String encoded = URLEncoder.encode(installer.getName(), "UTF-8");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + encoded + "\"");
            IOUtils.copyFile(resp.getOutputStream(), installer);
        } catch (BuildException ex) {
            getLogger().error(ex.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, URLEncoder.encode(ex.getMessage(), "UTF-8"));
        }
    }

    protected abstract SQLGLogger getLogger();
}
