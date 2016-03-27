package sample;

import server.core.AppInit;
import server.core.LoginData;
import sqlg2.db.SQLGLogger;
import sqlg2.db.SessionFactory;

public final class SampleInit implements AppInit {

    public SQLGLogger createLogger() {
        return new SQLGLogger.Simple();
    }

    public SessionFactory init(String application, LoginData data) {
        return new SimpleSessionFactory(data.getDriver(), data.getUrl());
    }

    public void destroy() {
    }
}
