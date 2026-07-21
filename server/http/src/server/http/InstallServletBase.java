package server.http;

import apploader.common.Application;
import server.core.AppLogger;
import server.install.BuildException;
import server.install.IOUtils;
import server.install.InstallBuilder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

public abstract class InstallServletBase extends AppServletBase {

    private static final class InstallKey {

        final String profile;
        final boolean zip;

        InstallKey(String profile, boolean zip) {
            this.profile = profile;
            this.zip = zip;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InstallKey) {
                InstallKey that = (InstallKey) obj;
                return Objects.equals(this.profile, that.profile) && this.zip == that.zip;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return profile.hashCode() + (zip ? 1 : 0);
        }

        @Override
        public String toString() {
            return (zip ? "Zip " : "") + profile;
        }
    }

    private volatile File root = new File(".");
    private final Map<InstallKey, InstallState> map = new HashMap<>();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        root = getRoot(config);
    }

    protected abstract File getRoot(ServletConfig config);

    private synchronized InstallState getBuilder(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String profile = pathInfo == null || pathInfo.isEmpty() ? "" : pathInfo.substring(1);
        boolean zip = Boolean.parseBoolean(req.getParameter("zip"));
        InstallKey key = new InstallKey(profile, zip);
        InstallState state = map.get(key);

        if (state == null) {
            List<String> apps = new ArrayList<>();
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
                url = req.getScheme() + "://" + req.getLocalName() + ":" + req.getLocalPort() + "/";
            }
            InstallBuilder builder = InstallBuilder.create(
                root, apps, key.profile, key.zip, url, getLogger()
            );
            state = new InstallState(builder);
            map.put(key, state);
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
        String json =
            "{" +
            "\"percent\": \"" + state.builder.getPercentCell().get() + "%\", " +
            "\"done\": " + done + ", " +
            "\"ok\": " + ok + ", " +
            "\"error\": \"" + state.error.replace('"', '\'') + "\"" +
            "}";
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

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        InstallState state = getBuilder(req);
        File installer = state.builder.getReadyInstaller();
        if (installer != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentLengthLong(installer.length());
            resp.setDateHeader("Last-Modified", installer.lastModified());
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected abstract AppLogger getLogger();
}
