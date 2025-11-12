package server.install;

public interface InstallLogger {

    void trace(String message);

    void error(Throwable error);
}
