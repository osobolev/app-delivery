package server.tomcat;

import server.http.Application;
import server.http.ListServletBase;

import javax.servlet.ServletConfig;
import java.util.List;

public final class ListServlet extends ListServletBase {

    protected List<Application> loadApplications(ServletConfig config) {
        return SingleUtil.getApplication(config);
    }
}
