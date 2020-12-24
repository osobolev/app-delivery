package sample;

import server.core.AppInit;
import server.core.AppLogger;
import server.core.LoginData;
import sqlg3.remote.server.IServerSerializer;
import sqlg3.remote.server.ServerJavaSerializer;
import sqlg3.runtime.GlobalContext;
import sqlg3.runtime.RuntimeMapperImpl;
import sqlg3.runtime.SqlTrace;
import sqlg3.runtime.specific.Generic;

public final class SampleInit implements AppInit {

    private final ServerJavaSerializer serializer = new ServerJavaSerializer();

    private final AppLogger logger;

    public SampleInit(AppLogger logger) {
        this.logger = logger;
    }

    public AppLogger createLogger() {
        return logger;
    }

    public IServerSerializer getSerializer() {
        return serializer;
    }

    @Override
    public InitData init(String application, LoginData data, SqlTrace trace) {
        GlobalContext global = new GlobalContext(new Generic(), new RuntimeMapperImpl(), trace);
        return new InitData(new SimpleSessionFactory(data.getDriver(), data.getUrl()), global);
    }

    public void destroy() {
    }
}
