package apploader.lib;

public interface IUpdateProgress {

    void setPercent(int percent);

    void done(String error);
}
