package server.http;

import apploader.common.ConfigReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public abstract class ListServletBase extends AppServletBase {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Application> applications = getApplications();
        resp.setCharacterEncoding(ConfigReader.CHARSET);
        resp.setContentType("text/plain");
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());
        for (Application application : applications) {
            resp.getWriter().println(application.id + "=" + application.name);
        }
    }
}
