package server.jetty;

import server.core.AppInit;
import server.core.LoginData;
import server.http.SessionUtil;
import sqlg2.db.HttpDispatcher;
import sqlg2.db.SQLGLogger;
import sqlg2.db.SessionFactory;
import sqlg2.db.SqlTrace;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class AppServerComponent {

    public final String application;
    private final String appName;
    private final AppInit init;

    private SQLGLogger logger;
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

    public void init(AppLogin login, SQLGLogger logger, SqlTrace trace) throws UserCancelException, ServerInitException {
        this.logger = logger;
        LoginData loginData = login.login(application);
        SessionFactory sf = init.init(application, loginData);

        this.http = new HttpDispatcher(application, sf, loginData.getSpecific(), logger);
        http.setSerializer(init.getSerializer());
        http.setSqlTrace(trace);
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
            HttpDispatcher.writeResponse(init.getSerializer(), os, null, new IOException("Server not running"));
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
    }
}
