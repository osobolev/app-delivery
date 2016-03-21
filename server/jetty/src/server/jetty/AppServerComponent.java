package server.jetty;

import server.core.AppAuthFactory;
import server.core.LoginData;
import server.http.SessionUtil;
import sqlg2.db.HttpDispatcher;
import sqlg2.db.SessionFactory;
import sqlg2.db.SimpleLogger;
import sqlg2.db.SqlTrace;
import sqlg2.db.specific.OracleDBSpecific;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class AppServerComponent {

    public final String application;
    private final String appName;

    private volatile boolean running = false;
    private HttpDispatcher http = null;
    private SimpleLogger logger;

    public AppServerComponent(String application, String appName) {
        this.application = application;
        this.appName = appName;
    }

    public String getName() {
        return appName;
    }

    public void init(AppLogin login, AppAuthFactory authFactory, SimpleLogger logger, SqlTrace trace) throws UserCancelException, ServerInitException {
        LoginData loginData = login.login(application);
        SessionFactory sf = authFactory.getAuthentificator(application, loginData);
        this.http = new HttpDispatcher(application, sf, new OracleDBSpecific(), logger);
        http.setSqlTrace(trace);
        this.logger = logger;
    }

    public void start() {
        running = true;
        logger.info("Сервер '" + appName + "' работает");
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
        logger.info("Сервер '" + appName + "' остановлен");
    }

    public void dispatch(String hostName, InputStream is, OutputStream os) throws IOException {
        if (!running) {
            HttpDispatcher.writeResponse(os, null, new IOException("Server not running"));
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
        logger.close();
    }
}
