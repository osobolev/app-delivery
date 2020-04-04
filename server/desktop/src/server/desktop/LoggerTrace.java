package server.desktop;

import sqlg3.remote.server.SQLGLogger;
import sqlg3.runtime.SqlTrace;

final class LoggerTrace {

    final SQLGLogger logger;
    final SqlTrace trace;

    LoggerTrace(SQLGLogger logger, SqlTrace trace) {
        this.logger = logger;
        this.trace = trace;
    }
}
