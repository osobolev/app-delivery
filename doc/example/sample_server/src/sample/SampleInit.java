package sample;

import server.core.AppInit;
import server.core.AppLogger;
import server.core.LoginData;
import sqlg3.runtime.GlobalContext;
import sqlg3.runtime.RuntimeMapperImpl;
import sqlg3.runtime.SqlTrace;
import sqlg3.runtime.specific.Generic;
import txrpc.runtime.TxRpcGlobalContext;

public final class SampleInit implements AppInit {

    private final AppLogger logger;

    public SampleInit(AppLogger logger) {
        this.logger = logger;
    }

    @Override
    public AppLogger createLogger() {
        return logger;
    }

    @Override
    public InitData init(String application, LoginData data) {
        GlobalContext global = new GlobalContext(new Generic(), new RuntimeMapperImpl(), SqlTrace.createDefault(logger::error));
        return new InitData(
            new SimpleSessionFactory(data.getDriver(), data.getUrl()),
            new TxRpcGlobalContext(global::newDaoInstance)
        );
    }

    @Override
    public void destroy() {
    }
}
