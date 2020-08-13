package server.install;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class PercentCell {

    private final AtomicInteger percentCell = new AtomicInteger();
    private final Consumer<String> logger;

    public PercentCell(Consumer<String> logger) {
        this.logger = logger;
    }

    public void setPercent(int newPercent) {
        int oldPercent = percentCell.get();
        if (newPercent > oldPercent) {
            int percent = Math.min(newPercent, 100);
            percentCell.set(percent);
            logger.accept(percent + "%");
        }
    }

    public void workPercent(double... values) {
        double realPercent = 0;
        double denom = 1;
        for (int i = 0; i < values.length; i += 2) {
            double current = values[i];
            double total = values[i + 1];
            double percent = current / total;
            realPercent += percent / denom;
            denom *= total;
        }
        int newPercent = (int) Math.round(realPercent * 100);
        setPercent(newPercent);
    }

    public void reset() {
        percentCell.set(0);
    }

    public int get() {
        return percentCell.get();
    }
}
