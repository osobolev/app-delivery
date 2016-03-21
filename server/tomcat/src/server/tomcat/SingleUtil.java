package server.tomcat;

import server.http.Application;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.List;

final class SingleUtil {

    static final String APPLICATION = "application";

    static List<Application> getApplication(ServletConfig config) {
        ServletContext ctx = config.getServletContext();
        String application = ctx.getInitParameter(APPLICATION);
        String appName = ctx.getInitParameter("appName");
        if (appName == null) {
            appName = application;
        }
        return Collections.singletonList(new Application(application, appName));
    }
}
