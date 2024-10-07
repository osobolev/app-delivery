package server.core;

import txrpc.remote.server.TxRpcLogger;

public interface AppLogger extends TxRpcLogger {

    void close();
}
