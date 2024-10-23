package server.embedded;

import server.core.AppInit;
import server.core.AppLogger;
import server.core.LoginData;
import server.http.ServletRequestFactory;
import txrpc.remote.server.HttpDispatcher;
import txrpc.remote.server.IHttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    public void init(AppLogin login, AppLogger logger) throws UserCancelException, ServerInitException {
        this.logger = logger;
        LoginData loginData = login.login(application);
        AppInit.InitData initData = init.init(application, loginData);
        this.http = new HttpDispatcher(initData.sessionFactory, logger, initData.global);
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
            request.writeError(new IllegalStateException("Server not running"));
        } else {
            http.dispatch(request);
        }
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
