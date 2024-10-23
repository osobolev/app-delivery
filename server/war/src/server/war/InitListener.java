package server.war;

import server.core.AppInit;
import server.core.AppLogger;
import server.core.LoginData;
import server.http.ServletRequestFactory;
import txrpc.remote.server.HttpDispatcher;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.SQLException;

public class InitListener implements ServletContextListener {

    private static final String APP_INIT_ATTR = "appInit";
    private static final String LOGGER_ATTR = "logger";
    private static final String FACTORY_ATTR = "requestFactory";
    private static final String DISPATCH_ATTR = "httpDispatch";

    private static LoginData getDbParameters(ServletContext ctx) {
        return new LoginData(
            ctx.getInitParameter("jdbcDriver"),
            ctx.getInitParameter("jdbcUrl"), ctx.getInitParameter("username"), ctx.getInitParameter("password")
        );
    }

    protected static AppInit getInit(ServletContext ctx) {
        String initClassName = ctx.getInitParameter("appInit");
        if (initClassName == null) {
            ctx.log("Не задан параметр appInit");
        } else {
            try {
                Object obj = Class.forName(initClassName).getDeclaredConstructor().newInstance();
                if (obj instanceof AppInit) {
                    return (AppInit) obj;
                } else if (obj instanceof AppInitFactory) {
                    AppInitFactory factory = (AppInitFactory) obj;
                    return factory.createInit(ctx);
                } else {
                    ctx.log("Объект " + initClassName + " должен быть либо AppInit, либо AppInitFactory");
                }
            } catch (Exception ex) {
                ctx.log("Невозможно создать объект " + initClassName, ex);
            }
        }
        return null;
    }

    protected static ServletRequestFactory createRequestFactory(ServletContext ctx) {
        String rfClassName = ctx.getInitParameter("requestFactory");
        if (rfClassName != null) {
            try {
                return (ServletRequestFactory) Class.forName(rfClassName).getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                ctx.log("Невозможно создать объект " + rfClassName, ex);
            }
        }
        return new DefaultRequestFactory();
    }

    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        AppInit init = getInit(ctx);
        AppLogger logger = init == null ? new ServletAppLogger(ctx) : init.createLogger();
        ctx.setAttribute(LOGGER_ATTR, logger);
        ServletRequestFactory requestFactory = createRequestFactory(ctx);
        ctx.setAttribute(FACTORY_ATTR, requestFactory);
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
        AppInit.InitData initData = init.init(application, data);
        HttpDispatcher http = new HttpDispatcher(initData.sessionFactory, logger, initData.global);
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
        AppLogger logger = getLogger(ctx);
        if (logger != null) {
            logger.close();
        }
    }

    static AppLogger getLogger(ServletContext ctx) {
        return (AppLogger) ctx.getAttribute(LOGGER_ATTR);
    }

    static ServletRequestFactory getRequestFactory(ServletContext ctx) {
        return (ServletRequestFactory) ctx.getAttribute(FACTORY_ATTR);
    }

    static HttpDispatcher getHttpDispatcher(ServletContext ctx) {
        return (HttpDispatcher) ctx.getAttribute(DISPATCH_ATTR);
    }
}
