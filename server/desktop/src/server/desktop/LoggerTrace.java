package server.desktop;

import server.core.AppLogger;
import sqlg3.runtime.SqlTrace;

final class LoggerTrace {

    final AppLogger logger;
    final SqlTrace trace;

    LoggerTrace(AppLogger logger, SqlTrace trace) {
        this.logger = logger;
        this.trace = trace;
    }
}
