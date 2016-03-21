package server.install;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

final class CountOutputEater implements Runnable {

    private final BufferedReader in;
    private final int total;
    private final InstallBuilder builder;

    CountOutputEater(InputStream in, int total, InstallBuilder builder) {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.total = total;
        this.builder = builder;
    }

    public void run() {
        int count = 0;
        while (true) {
            try {
                String str = in.readLine();
                if (str == null)
                    break;
                builder.workPercent(1, 2, count++, total);
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}
