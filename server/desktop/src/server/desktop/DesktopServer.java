package server.desktop;

import server.jetty.AppLogin;
import server.jetty.AppServerComponent;
import server.jetty.JettyHttpContainer;

import javax.swing.*;
import java.io.File;
import java.util.List;

public final class DesktopServer {

    private final JettyHttpContainer container = new JettyHttpContainer();

    public void showError(String message) {
        container.error(message);
        ServerFrame.showError(message);
    }

    public void showError(Throwable ex) {
        container.error(ex);
        ServerFrame.showError(ex.toString());
    }

    public void runServer(String[] args, AppLogin login, List<AppServerComponent> comps) {
        FrameBuilder builder = new FrameBuilder(container);
        builder.build(args, login, comps);
        showFrame(builder);
    }

    public void runServer(int port, File rootDir, AppLogin login, List<AppServerComponent> comps) {
        FrameBuilder builder = new FrameBuilder(container);
        builder.build(port, rootDir, login, comps);
        showFrame(builder);
    }

    private void showFrame(final FrameBuilder builder) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ServerFrame(container, builder.tab);
            }
        });
    }
}
