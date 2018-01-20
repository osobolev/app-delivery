package server.war;

import apploader.common.Application;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.List;

final class SingleUtil {

    private static final String APPLICATION = "application";

    static String getApplication(ServletContext ctx) {
        return ctx.getInitParameter(APPLICATION);
    }

    static List<Application> getApplications(ServletConfig config) {
        ServletContext ctx = config.getServletContext();
        String application = getApplication(ctx);
        String appName = ctx.getInitParameter("appName");
        if (appName == null) {
            appName = application;
        }
        return Collections.singletonList(new Application(application, appName));
    }
}
