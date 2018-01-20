package server.war;

import apploader.common.Application;
import server.http.ListServletBase;

import javax.servlet.ServletConfig;
import java.util.List;

public class ListServlet extends ListServletBase {

    protected List<Application> loadApplications(ServletConfig config) {
        return SingleUtil.getApplications(config);
    }
}
