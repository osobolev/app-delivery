package server.core;

import sqlg3.remote.server.IServerSerializer;
import sqlg3.remote.server.SessionFactory;
import sqlg3.runtime.GlobalContext;
import sqlg3.runtime.SqlTrace;

public interface AppInit {

    AppLogger createLogger();

    IServerSerializer getSerializer();

    final class InitData {

        public final SessionFactory sessionFactory;
        public final GlobalContext global;

        public InitData(SessionFactory sessionFactory, GlobalContext global) {
            this.sessionFactory = sessionFactory;
            this.global = global;
        }
    }

    InitData init(String application, LoginData login, SqlTrace trace);

    void destroy();
}
