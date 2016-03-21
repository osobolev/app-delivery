package server.install;

public final class BuildException extends Exception {

    BuildException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
