package server.install;

import java.io.IOException;
import java.io.InputStream;

public final class OutputEater implements Runnable {

    private final InputStream in;

    public OutputEater(InputStream in) {
        this.in = in;
    }

    public void run() {
        while (true) {
            try {
                int c = in.read();
                if (c < 0)
                    break;
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}
