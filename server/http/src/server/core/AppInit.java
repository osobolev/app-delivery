package server.core;

import txrpc.runtime.SessionFactory;
import txrpc.runtime.TxRpcGlobalContext;

import java.util.Set;

public interface AppInit {

    AppLogger createLogger();

    final class InitData {

        public final SessionFactory sessionFactory;
        public final TxRpcGlobalContext global;
        public final Set<String> blacklist;
        public final Set<String> whitelist;

        public InitData(SessionFactory sessionFactory, TxRpcGlobalContext global) {
            this(sessionFactory, global, null, null);
        }

        public InitData(SessionFactory sessionFactory, TxRpcGlobalContext global,
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
