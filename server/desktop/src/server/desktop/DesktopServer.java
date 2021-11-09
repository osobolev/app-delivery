package server.desktop;

import server.core.AppLogger;
import server.embedded.*;

import javax.swing.*;
import java.io.File;
import java.util.List;

public final class DesktopServer {

    private final EmbeddedHttpContainer container;

    public DesktopServer(AppLogger mainLogger, EmbeddedServer server) {
        this.container = new EmbeddedHttpContainer(mainLogger, server);
    }

    public DesktopServer(EmbeddedServer server) {
        this(new EmbeddedLogger(), server);
    }

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

    public void runServer(Integer port, String context, File rootDir, AppLogin login, List<AppServerComponent> comps) {
        FrameBuilder builder = new FrameBuilder(container);
        builder.build(port, context, rootDir, login, comps);
        showFrame(builder);
    }

    private void showFrame(FrameBuilder builder) {
        SwingUtilities.invokeLater(() -> new ServerFrame(container, builder.tab));
    }
}
