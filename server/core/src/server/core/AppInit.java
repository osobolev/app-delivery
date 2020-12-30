package server.core;

import sqlg3.remote.server.IServerSerializer;
import sqlg3.remote.server.SessionFactory;
import sqlg3.runtime.GlobalContext;
import sqlg3.runtime.SqlTrace;

import java.util.Set;

public interface AppInit {

    AppLogger createLogger();

    IServerSerializer getSerializer();

    final class InitData {

        public final SessionFactory sessionFactory;
        public final GlobalContext global;
        public final Set<String> blacklist;
        public final Set<String> whitelist;

        public InitData(SessionFactory sessionFactory, GlobalContext global) {
            this(sessionFactory, global, null, null);
        }

        public InitData(SessionFactory sessionFactory, GlobalContext global,
                        Set<String> blacklist, Set<String> whitelist) {
            this.sessionFactory = sessionFactory;
            this.global = global;
            this.blacklist = blacklist;
            this.whitelist = whitelist;
        }
    }

    InitData init(String application, LoginData login, SqlTrace trace);

    void destroy();
}
