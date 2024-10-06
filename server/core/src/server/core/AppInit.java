package server.core;

import sqlg3.remote.server.SessionFactory;
import sqlg3.tx.runtime.TransGlobalContext;

import java.util.Set;

public interface AppInit {

    AppLogger createLogger();

    final class InitData {

        public final SessionFactory sessionFactory;
        public final TransGlobalContext global;
        public final Set<String> blacklist;
        public final Set<String> whitelist;

        public InitData(SessionFactory sessionFactory, TransGlobalContext global) {
            this(sessionFactory, global, null, null);
        }

        public InitData(SessionFactory sessionFactory, TransGlobalContext global,
                        Set<String> blacklist, Set<String> whitelist) {
            this.sessionFactory = sessionFactory;
            this.global = global;
            this.blacklist = blacklist;
            this.whitelist = whitelist;
        }
    }

    InitData init(String application, LoginData login);

    void destroy();
}
