package server.desktop;

import server.core.AppAuthFactory;
import server.jetty.*;
import sqlg2.db.SimpleLogger;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class FrameBuilder {

    private final JettyHttpContainer container;
    final JTabbedPane tab = new JTabbedPane();

    FrameBuilder(JettyHttpContainer container) {
        this.container = container;
    }

    void build(String[] args, AppLogin login, AppAuthFactory authFactory, List<AppServerComponent> comps) {
        HttpConfig config;
        try {
            config = HttpConfig.parse(args);
        } catch (ServerInitException ex) {
            container.error(ex.getMessage());
            ServerFrame.showError(ex.getMessage());
            System.exit(1);
            return;
        }
        int port;
        if (config.port == null) {
            port = container.getDefaultPort(config.rootDir);
        } else {
            port = config.port.intValue();
        }
        build(port, config.rootDir, login, authFactory, comps);
    }

    void build(int port, File rootDir, AppLogin login, AppAuthFactory authFactory, List<AppServerComponent> comps) {
        if (!rootDir.isDirectory()) {
            String message = "'" + rootDir + "' - не каталог";
            container.error(message);
            ServerFrame.showError(message);
            System.exit(1);
        }
        List<ComponentPanel> panels = new ArrayList<ComponentPanel>();
        int index = 0;
        for (AppServerComponent comp : comps) {
            String name = comp.getName();
            JLabel lbl = new JLabel(name);
            ComponentPanel panel = new ComponentPanel(comp, lbl);
            SimpleLogger logger = panel.getLogger();
            boolean inited = false;
            try {
                comp.init(login, authFactory, logger, panel.getTrace());

                tab.addTab(name, panel);
                tab.setTabComponentAt(index, lbl);
                container.addApplication(comp);
                panels.add(panel);
                index++;

                inited = true;
            } catch (UserCancelException ex) {
                // ignore
            } catch (ServerInitException ex) {
                logger.error(ex);
                ServerFrame.showError("Ошибка при инициализации " + name + ": " + ex.getMessage());
            } finally {
                if (!inited) {
                    logger.close();
                }
            }
        }

        if (container.getComponents() <= 0) {
            ServerFrame.showError("Нет ни одного серверного компонента");
            System.exit(3);
        }

        try {
            container.init(port, rootDir);
            container.start();
        } catch (Exception ex) {
            container.error(ex);
            ServerFrame.showError(ex.toString());
            System.exit(2);
            return;
        }
        for (ComponentPanel panel : panels) {
            panel.start();
        }
    }
}
