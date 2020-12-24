package sample;

import server.core.AppLogger;
import sqlg3.remote.server.SQLGLogger;

public final class SampleLogger extends SQLGLogger.Simple implements AppLogger {

    @Override
    public void close() {
    }
}
