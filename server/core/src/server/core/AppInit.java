package server.core;

import sqlg3.remote.common.SQLGLogger;
import sqlg3.remote.server.IServerSerializer;
import sqlg3.remote.server.SessionFactory;

public interface AppInit {

    SQLGLogger createLogger();

    IServerSerializer getSerializer();

    SessionFactory init(String application, LoginData login);

    void destroy();
}
