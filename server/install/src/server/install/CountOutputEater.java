package server.install;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public final class CountOutputEater implements Runnable {

    private final BufferedReader in;
    private final int total;
    private final PercentCell percentCell;

    public CountOutputEater(InputStream in, int total, PercentCell percentCell, Charset charset) {
        this.in = new BufferedReader(new InputStreamReader(in, charset));
        this.total = total;
        this.percentCell = percentCell;
    }

    public void run() {
        int count = 0;
        while (true) {
            try {
                String str = in.readLine();
                if (str == null)
                    break;
                percentCell.workPercent(1, 2, count++, total);
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}
