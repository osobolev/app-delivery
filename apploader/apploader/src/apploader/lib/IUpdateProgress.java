package apploader.lib;

public interface IUpdateProgress extends AutoCloseable {

    void setPercent(int percent);

    void close();
}
