package server.core;

import sqlg2.db.IServerSerializer;
import sqlg2.db.SQLGLogger;
import sqlg2.db.SessionFactory;

public interface AppInit {

    SQLGLogger createLogger();

    IServerSerializer getSerializer();

    SessionFactory init(String application, LoginData login);

    void destroy();
}
