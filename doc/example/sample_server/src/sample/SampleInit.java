package sample;

import server.core.AppInit;
import server.core.LoginData;
import sqlg3.remote.common.SQLGLogger;
import sqlg3.remote.server.IServerSerializer;
import sqlg3.remote.server.ServerJavaSerializer;
import sqlg3.remote.server.SessionFactory;

public final class SampleInit implements AppInit {

    private final ServerJavaSerializer serializer = new ServerJavaSerializer();

    public SQLGLogger createLogger() {
        return new SQLGLogger.Simple();
    }

    public IServerSerializer getSerializer() {
        return serializer;
    }

    public SessionFactory init(String application, LoginData data) {
        return new SimpleSessionFactory(data.getDriver(), data.getUrl());
    }

    public void destroy() {
    }
}
