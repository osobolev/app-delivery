package sample;

import server.core.AppLogger;
import txrpc.remote.server.TxRpcLogger;

public final class SampleLogger extends TxRpcLogger.Simple implements AppLogger {

    @Override
    public void close() {
    }
}
