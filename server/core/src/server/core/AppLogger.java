package server.core;

import txrpc.runtime.TxRpcLogger;

public interface AppLogger extends TxRpcLogger {

    void close();
}
