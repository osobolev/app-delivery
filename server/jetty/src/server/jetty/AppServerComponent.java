package server.jetty;

import server.core.AppInit;
import server.core.AppLogger;
import server.core.LoginData;
import server.http.SessionUtil;
import sqlg3.remote.server.HttpDispatcher;
import sqlg3.runtime.SqlTrace;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class AppServerComponent {

    public final String application;
    private final String appName;
    private final AppInit init;

    private AppLogger logger;
    private volatile boolean running = false;
    private HttpDispatcher http = null;

    public AppServerComponent(String application, String appName, AppInit init) {
        this.application = application;
        this.appName = appName;
        this.init = init;
    }

    public String getName() {
        return appName;
    }

    public AppInit getInit() {
        return init;
    }

    public void init(AppLogin login, AppLogger logger, SqlTrace trace) throws UserCancelException, ServerInitException {
        this.logger = logger;
        LoginData loginData = login.login(application);
        AppInit.InitData initData = init.init(application, loginData, trace);
        this.http = new HttpDispatcher(application, initData.sessionFactory, logger, initData.global);
        http.setSerializer(init.getSerializer());
    }

    public void start() {
        running = true;
        if (logger != null) {
            logger.info("Сервер '" + appName + "' работает");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
        if (logger != null) {
            logger.info("Сервер '" + appName + "' остановлен");
        }
    }

    public void dispatch(String hostName, InputStream is, OutputStream os) throws IOException {
        if (!running) {
            HttpDispatcher.writeError(init.getSerializer(), os, new IOException("Server not running"));
        } else {
            http.dispatch(hostName, is, os);
        }
    }

    public void showSessions(OutputStream os) throws IOException {
        SessionUtil.writeSessionInfo(os, http, appName);
    }

    public void shutdown() {
        stop();
        if (http != null) {
            http.shutdown();
        }
        init.destroy();
        if (logger != null) {
            logger.close();
        }
    }
}
