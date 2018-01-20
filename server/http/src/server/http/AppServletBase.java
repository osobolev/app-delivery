package server.http;

import apploader.common.Application;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Collections;
import java.util.List;

public abstract class AppServletBase extends HttpServlet {

    private volatile List<Application> applications = Collections.emptyList();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        applications = loadApplications(config);
    }

    protected abstract List<Application> loadApplications(ServletConfig config);

    protected final List<Application> getApplications() {
        return applications;
    }
}
