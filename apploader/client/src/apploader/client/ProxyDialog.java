package apploader.client;

import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.*;
import java.util.List;

public final class ProxyDialog extends JDialog {

    public interface ErrorShow {

        void showError(String message);
    }

    private final ErrorShow error;
    private final JCheckBox cbUseProxy = new JCheckBox("Использовать прокси-сервер");
    private final JComboBox chType = new JComboBox(new Proxy.Type[] {Proxy.Type.HTTP, Proxy.Type.SOCKS});
    private final JTextField tfHost = new JTextField(20);
    private final JTextField tfPort = new JTextField(5);
    private final JTextField tfUser = new JTextField(15);
    private final JPasswordField tfPass = new JPasswordField(15);

    private ProxyConfig resultProxy = null;

    public ProxyDialog(Component owner, ProxyConfig proxy, ErrorShow error) {
        this(owner, proxy, AppInfo.httpServerUrl, error);
    }

    public ProxyDialog(Component owner, ProxyConfig proxy, URL url, ErrorShow error) {
        super(owner == null ? null : SwingUtilities.getWindowAncestor(owner), "Настройки прокси", ModalityType.DOCUMENT_MODAL);
        this.error = error;

        if (proxy != null && proxy.proxy.type() != Proxy.Type.DIRECT) {
            cbUseProxy.setSelected(true);
            setProxy(proxy);
        } else {
            cbUseProxy.setSelected(false);
            tfPort.setText("80");
            try {
                URI uri;
                if (url == null) {
                    uri = new URI("http://example.com");
                } else {
                    uri = url.toURI();
                }
                List<Proxy> proxies = ProxySelector.getDefault().select(uri);
                for (Proxy defProxy : proxies) {
                    if (defProxy.type() == Proxy.Type.DIRECT)
                        continue;
                    setProxy(new ProxyConfig(defProxy, null, null));
                    break;
                }
            } catch (Exception ex) {
                // ignore
            }
        }

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        AbstractAction closeAction = new AbstractAction("Отмена") {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        rootPane.getActionMap().put("close", closeAction);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel up = new JPanel(new FlowLayout(FlowLayout.LEFT));
        up.add(cbUseProxy);
        add(up, BorderLayout.NORTH);

        JPanel main1 = new JPanel();
        main1.add(new JLabel("Тип:"));
        main1.add(chType);
        main1.add(new JLabel("Адрес:"));
        main1.add(tfHost);
        main1.add(new JLabel("Порт:"));
        main1.add(tfPort);
        JPanel main2 = new JPanel();
        main2.add(new JLabel("Пользователь:"));
        main2.add(tfUser);
        main2.add(new JLabel("Пароль:"));
        main2.add(tfPass);
        JPanel main = new JPanel(new BorderLayout());
        main.add(main1, BorderLayout.CENTER);
        main.add(main2, BorderLayout.SOUTH);
        add(main, BorderLayout.CENTER);

        cbUseProxy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableDisable();
            }
        });
        enableDisable();

        JPanel butt = new JPanel(new BasicOptionPaneUI.ButtonAreaLayout(true, 5));
        JButton btnOk = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                resultProxy = parseProxy();
                if (resultProxy != null) {
                    AppInfo.storeProxy(resultProxy);
                    dispose();
                }
            }
        });
        JButton btnCancel = new JButton(closeAction);
        butt.add(btnOk);
        butt.add(btnCancel);
        butt.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        add(butt, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setProxy(ProxyConfig proxy) {
        chType.setSelectedItem(proxy.proxy.type());
        InetSocketAddress address = (InetSocketAddress) proxy.proxy.address();
        tfHost.setText(address.getHostName());
        tfPort.setText(String.valueOf(address.getPort()));
        tfUser.setText(proxy.login);
        tfPass.setText(proxy.password);
    }

    private void showError(String message) {
        error.showError(message);
    }

    private void enableDisable() {
        boolean enable = cbUseProxy.isSelected();
        chType.setEnabled(enable);
        tfHost.setEnabled(enable);
        tfPort.setEnabled(enable);
        tfUser.setEnabled(enable);
        tfPass.setEnabled(enable);
    }

    private ProxyConfig parseProxy() {
        if (!cbUseProxy.isSelected())
            return ProxyConfig.NO_PROXY;
        if (tfHost.getText().trim().isEmpty()) {
            showError("Введите адрес прокси-сервера");
            return null;
        }
        String host = tfHost.getText();
        if (tfPort.getText().trim().isEmpty()) {
            showError("Введите номер порта прокси-сервера");
            return null;
        }
        int port;
        try {
            port = Integer.parseInt(tfPort.getText());
        } catch (NumberFormatException nfex) {
            showError("Введите число для порта прокси-сервера");
            return null;
        }
        InetSocketAddress address = InetSocketAddress.createUnresolved(host, port);
        Proxy.Type type = (Proxy.Type) chType.getSelectedItem();
        return new ProxyConfig(new Proxy(type, address), tfUser.getText(), new String(tfPass.getPassword()));
    }

    public ProxyConfig getProxy() {
        return resultProxy;
    }
}
