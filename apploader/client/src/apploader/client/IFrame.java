package apploader.client;

public interface IFrame {

    boolean isShowing();

    void showInFront();

    boolean onClosing();

    void dispose();
}
