package server.embedded;

public final class ServerInitException extends Exception {

    public ServerInitException(String message) {
        super(message);
    }

    public ServerInitException(Throwable cause) {
        super(cause);
    }
}
