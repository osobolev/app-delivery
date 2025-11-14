package server.install;

public interface InstallLogger {

    void trace(String message);

    default void traceProgress(String message) {
        trace(message);
    }

    void error(Throwable error);
}
