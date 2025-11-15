package server.install;

public interface InstallLogger {

    void trace(String message);

    default void traceProgress(int percent) {
        trace(percent + "%");
    }

    void error(Throwable error);
}
