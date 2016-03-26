package server.war;

import server.http.Application;
import server.http.ListServletBase;

import javax.servlet.ServletConfig;
import java.util.List;

public class ListServlet extends ListServletBase {

    protected List<Application> loadApplications(ServletConfig config) {
        return SingleUtil.getApplications(config);
    }
}
