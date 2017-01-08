package server.desktop;

import server.jetty.JettyHttpContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

final class ServerFrame extends JFrame {

    private final JettyHttpContainer container;

    ServerFrame(JettyHttpContainer container, JTabbedPane tab) {
        super("Сервер");
        this.container = container;

        add(tab, BorderLayout.CENTER);
        JButton btnStop = new JButton(new AbstractAction("Выход") {
            public void actionPerformed(ActionEvent e) {
                actionClose();
            }
        });
        JPanel down = new JPanel();
        down.add(btnStop);
        add(down, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actionClose();
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        pack();
        setVisible(true);
    }

    static void showError(String str) {
        showError(null, str);
    }

    private static void showError(JFrame frame, String str) {
        JOptionPane.showMessageDialog(frame, str, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private boolean onClosing() {
        int ans = JOptionPane.showConfirmDialog(
            this, "Действительно закрыть?", "Подтверждение", JOptionPane.OK_CANCEL_OPTION
        );
        return ans == JOptionPane.YES_OPTION;
    }

    private void actionClose() {
        if (onClosing()) {
            try {
                container.stop();
            } catch (Exception ex) {
                container.error(ex);
                showError(this, ex.toString());
            }
            dispose();
            System.exit(0);
        }
    }
}
