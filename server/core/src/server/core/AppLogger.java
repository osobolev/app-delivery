package server.core;

import sqlg3.remote.server.SQLGLogger;

public interface AppLogger extends SQLGLogger {

    void close();
}
