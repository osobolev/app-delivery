package server.war;

import server.core.AppInit;
import server.core.LoginData;
import sqlg2.db.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.SQLException;

public class InitListener implements ServletContextListener {

    private static final String APP_INIT_ATTR = "appInit";
    private static final String LOGGER_ATTR = "logger";
    private static final String SERIALIZER_ATTR = "serializer";
    private static final String DISPATCH_ATTR = "httpDispatch";

    private static LoginData getDbParameters(ServletContext ctx) throws Exception {
        String driver = LoginData.getDriver(ctx.getInitParameter("jdbcDriver"));
        DBSpecific specific = LoginData.getSpecific(ctx.getInitParameter("dbSpec"));
        return new LoginData(
            driver,
            ctx.getInitParameter("jdbcUrl"), ctx.getInitParameter("username"), ctx.getInitParameter("password"),
            specific
        );
    }

    protected AppInit getInit(ServletContext ctx) {
        String initClassName = ctx.getInitParameter("appInit");
        if (initClassName == null) {
            initClassName = ctx.getInitParameter("authFactory");
        }
        if (initClassName == null) {
            ctx.log("Не задан параметр appInit");
        } else {
            try {
                return (AppInit) Class.forName(initClassName).newInstance();
            } catch (Exception ex) {
                ctx.log("Невозможно создать объект " + initClassName, ex);
            }
        }
        return null;
    }

    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        AppInit init = getInit(ctx);
        SQLGLogger logger = init == null ? new SQLGLogger.Simple() : init.createLogger();
        ctx.setAttribute(LOGGER_ATTR, logger);
        IServerSerializer serializer = init == null ? new ServerJavaSerializer() : init.getSerializer();
        ctx.setAttribute(SERIALIZER_ATTR, serializer);
        if (init == null)
            return;
        ctx.setAttribute(APP_INIT_ATTR, init);

        LoginData data = null;
        try {
            data = getDbParameters(ctx);
        } catch (Exception ex) {
            logger.error(ex);
        }
        if (data == null)
            return;

        try {
            data.testConnection();
        } catch (SQLException ex) {
            logger.error("CANNOT CONNECT TO DB: " + ex);
        }

        String application = SingleUtil.getApplication(ctx);
        SessionFactory sf = init.init(application, data);
        HttpDispatcher http = new HttpDispatcher(application, sf, data.getSpecific(), logger);
        ctx.setAttribute(DISPATCH_ATTR, http);
    }

    public void contextDestroyed(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        AppInit init = (AppInit) ctx.getAttribute(APP_INIT_ATTR);
        if (init != null) {
            init.destroy();
        }
        HttpDispatcher http = getHttpDispatcher(ctx);
        if (http != null) {
            http.shutdown();
        }
    }

    static SQLGLogger getLogger(ServletContext ctx) {
        return (SQLGLogger) ctx.getAttribute(LOGGER_ATTR);
    }

    static IServerSerializer getSerializer(ServletContext ctx) {
        return (IServerSerializer) ctx.getAttribute(SERIALIZER_ATTR);
    }

    static HttpDispatcher getHttpDispatcher(ServletContext ctx) {
        return (HttpDispatcher) ctx.getAttribute(DISPATCH_ATTR);
    }
}
