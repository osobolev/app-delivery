package server.embedded;

import apploader.common.Application;
import server.http.ListServletBase;

import javax.servlet.ServletConfig;
import java.util.List;

final class ListServlet extends ListServletBase {

    private final List<Application> applications;

    ListServlet(List<Application> applications) {
        this.applications = applications;
    }

    protected List<Application> loadApplications(ServletConfig config) {
        return applications;
    }
}
