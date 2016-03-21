package server.jetty;

import org.eclipse.jetty.util.log.Logger;
import sqlg2.db.SimpleLogger;

public final class HttpLogger implements Logger {

    private final SimpleLogger logger;

    public HttpLogger(SimpleLogger logger) {
        this.logger = logger;
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void setDebugEnabled(boolean on) {
    }

    public String getName() {
        return "AppServerLog";
    }

    public void warn(String msg, Object... args) {
        logger.error(format(msg, args));
    }

    public void warn(Throwable error) {
        logger.error(error);
    }

    public void warn(String msg, Throwable error) {
        if (msg != null) {
            logger.error(msg);
        }
        if (error != null) {
            logger.error(error);
        }
    }

    public void info(String msg, Object... args) {
        logger.info(format(msg, args));
    }

    public void info(Throwable error) {
        logger.error(error);
    }

    public void info(String msg, Throwable error) {
        if (msg != null) {
            logger.info(msg);
        }
        if (error != null) {
            logger.error(error);
        }
    }

    public void debug(String msg, Object... args) {
    }

    public void debug(Throwable error) {
    }

    public void debug(String msg, Throwable error) {
    }

    public void ignore(Throwable error) {
    }

    private static String format(String msg, Object... args) {
        StringBuilder buf = new StringBuilder();
        String braces = "{}";
        int start = 0;
        for (Object arg : args) {
            int pos = msg.indexOf(braces, start);
            if (pos < 0) {
                buf.append(msg.substring(start));
                buf.append(' ').append(arg);
                start = msg.length();
            } else {
                buf.append(msg.substring(start, pos));
                buf.append(arg);
                start = pos + braces.length();
            }
        }
        return buf.toString();
    }

    public Logger getLogger(String name) {
        return this;
    }
}
