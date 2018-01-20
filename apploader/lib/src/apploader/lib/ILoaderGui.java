package apploader.lib;

public interface ILoaderGui {

    void showStatus(String status);

    void showWarning(String message);

    void showError(String message);

    Result showError2(String message, FileLoader loader);

    Result showWarning2(String message, FileLoader loader);

    Result showWarning3(String message, FileLoader loader);
}
