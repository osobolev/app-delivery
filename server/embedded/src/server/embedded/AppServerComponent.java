package server.embedded;

import server.core.AppInit;
import server.core.AppLogger;
import server.core.LoginData;
import server.http.ServletRequestFactory;
import server.http.SessionUtil;
import sqlg3.remote.server.HttpDispatcher;
import sqlg3.remote.server.IHttpRequest;
import sqlg3.runtime.SqlTrace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public final class AppServerComponent {

    public final String application;
    private final String appName;
    private final ServletRequestFactory requestFactory;
    private final AppInit init;

    private AppLogger logger;
    private volatile boolean running = false;
    private HttpDispatcher http = null;
    private Set<String> blacklist = null;
    private Set<String> whitelist = null;

    public AppServerComponent(String application, String appName, ServletRequestFactory requestFactory, AppInit init) {
        this.application = application;
        this.appName = appName;
        this.requestFactory = requestFactory;
        this.init = init;
    }

    public String getName() {
        return appName;
    }

    public AppInit getInit() {
        return init;
    }

    public boolean accept(String addr) {
        if (blacklist != null) {
            if (blacklist.contains(addr))
                return false;
        }
        if (whitelist != null) {
            if (!whitelist.contains(addr))
                return false;
        }
        return true;
    }

    public void error(String str) {
        if (logger != null) {
            logger.error(str);
        }
    }

    public void init(AppLogin login, AppLogger logger, SqlTrace trace) throws UserCancelException, ServerInitException {
        this.logger = logger;
        LoginData loginData = login.login(application);
        AppInit.InitData initData = init.init(application, loginData, trace);
        this.http = new HttpDispatcher(application, initData.sessionFactory, logger, initData.global);
        this.blacklist = initData.blacklist;
        this.whitelist = initData.whitelist;
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

    public void dispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        IHttpRequest request = requestFactory.newRequest(req, resp);
        if (!running) {
            request.writeError(new IOException("Server not running"));
        } else {
            http.dispatch(request);
        }
    }

    public void showSessions(OutputStream os) throws IOException {
        SessionUtil.writeSessionInfo(os, http);
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
