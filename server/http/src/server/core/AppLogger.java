package server.core;

import server.install.InstallLogger;
import txrpc.runtime.TxRpcLogger;

public interface AppLogger extends InstallLogger, TxRpcLogger {

    void close();
}
