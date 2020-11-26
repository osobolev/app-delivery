package server.war;

import server.core.AppLogger;

import javax.servlet.ServletContext;

final class ServletAppLogger implements AppLogger {

    private final ServletContext ctx;

    ServletAppLogger(ServletContext ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @Override
    public void trace(String message) {
        System.out.println(message);
    }

    @Override
    public void info(String message) {
        ctx.log(message);
    }

    @Override
    public void error(String message) {
        ctx.log(message, null);
    }

    @Override
    public void error(Throwable error) {
        ctx.log(error.getMessage(), error);
    }

    @Override
    public void close() {
    }
}
