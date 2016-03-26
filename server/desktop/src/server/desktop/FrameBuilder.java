package server.desktop;

import server.core.AppInit;
import server.jetty.*;
import sqlg2.db.SQLGLogger;

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

    void build(String[] args, AppLogin login, List<AppServerComponent> comps) {
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
        build(port, config.rootDir, login, comps);
    }

    void build(int port, File rootDir, AppLogin login, List<AppServerComponent> comps) {
        if (!rootDir.isDirectory()) {
            String message = "'" + rootDir + "' - �� �������";
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
            boolean inited = false;
            AppInit appInit = comp.getInit();
            SQLGLogger logger = panel.wrap(appInit.createLogger());
            try {
                comp.init(login, logger, panel.getTrace());

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
                ServerFrame.showError("������ ��� ������������� " + name + ": " + ex.getMessage());
            } finally {
                if (!inited) {
                    comp.shutdown();
                }
            }
        }

        if (container.getComponents() <= 0) {
            ServerFrame.showError("��� �� ������ ���������� ����������");
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
