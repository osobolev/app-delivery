package apploader;

import apploader.common.ProxyConfig;

import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.function.Consumer;

final class UrlDialog extends JDialog {

    interface ErrorShow {

        void showError(Component owner, String message);
    }

    private final ProxyConfig proxy;
    private final Consumer<Throwable> logError;
    private final ErrorShow error;
    private final JTextField tfUrl = new JTextField(30);

    private URL resultURL = null;

    UrlDialog(Component owner, ProxyConfig proxy, Consumer<Throwable> logError, ErrorShow error) {
        super(owner == null ? null : SwingUtilities.getWindowAncestor(owner), "Адрес сервера", ModalityType.DOCUMENT_MODAL);
        this.proxy = proxy;
        this.logError = logError;
        this.error = error;

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        AbstractAction closeAction = new AbstractAction("Отмена") {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        rootPane.getActionMap().put("close", closeAction);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel up = new JPanel(new FlowLayout(FlowLayout.LEFT));
        up.add(new JLabel("Не задан адрес сервера. Введите адрес (пример: http://my-server:8880)"));
        add(up, BorderLayout.NORTH);

        JPanel main = new JPanel();
        main.add(new JLabel("Адрес сервера:"));
        main.add(tfUrl);
        add(main, BorderLayout.CENTER);

        JPanel butt = new JPanel(new BasicOptionPaneUI.ButtonAreaLayout(true, 5));
        JButton btnOk = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                resultURL = parseURL();
                if (resultURL != null) {
                    dispose();
                }
            }
        });
        JButton btnCancel = new JButton(closeAction);
        butt.add(btnOk);
        butt.add(btnCancel);
        butt.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        add(butt, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showError(String message) {
        error.showError(this, message);
    }

    private URL parseURL() {
        String urlStr = tfUrl.getText().trim();
        if (urlStr.isEmpty()) {
            showError("Введите адрес сервера");
            return null;
        }
        try {
            return HeadlessGui.checkURL(proxy, urlStr);
        } catch (Exception ex) {
            logError.accept(ex);
            showError("Введите корректный адрес сервера");
            return null;
        }
    }

    URL getURL() {
        return resultURL;
    }
}
