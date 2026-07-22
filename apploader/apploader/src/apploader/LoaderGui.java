package apploader;

import apploader.client.ProxyDialog;
import apploader.client.SplashStatus;
import apploader.common.ProxyConfig;
import apploader.lib.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.List;

final class LoaderGui implements ILoaderGui {

    private static final String WARNING = "Предупреждение";
    private static final String ERROR = "Ошибка";

    private boolean lnfSet = false;

    public void showStatus(String status) {
        SplashStatus.setStatus(status);
    }

    private Result showDialogInternal(Component parent,
                                      String message,
                                      String title, int style,
                                      String[] optionNames,
                                      Result[] retValues,
                                      FileLoader check, FileLoader proxy) {
        setLnF();
        JButton[] options = new JButton[optionNames.length];
        JPanel panel = new JPanel(new BasicOptionPaneUI.ButtonAreaLayout(true, 5));
        JButton wideButton = new JButton("Отмена");
        int prefWidth = wideButton.getPreferredSize().width;
        for (int i = 0; i < optionNames.length; i++) {
            options[i] = new JButton(optionNames[i]);
            Dimension size = options[i].getPreferredSize();
            if (size.width < prefWidth) {
                size.width = prefWidth;
                options[i].setPreferredSize(size);
            }
            panel.add(options[i]);
        }
        JCheckBox cbShow = new JCheckBox("Не показывать больше", false);
        if (check != null || proxy != null) {
            JPanel chk = new JPanel(new BorderLayout());
            JPanel optPanel = new JPanel();
            chk.add(panel, BorderLayout.SOUTH);
            chk.add(optPanel, BorderLayout.CENTER);
            if (check != null) {
                optPanel.add(cbShow);
            }
            if (proxy != null) {
                JButton btnProxy = new JButton(new AbstractAction("Изменить прокси") {
                    public void actionPerformed(ActionEvent e) {
                        showProxyDialog(chk, proxy.getProxy(), proxy.getUrl(), proxy);
                    }
                });
                optPanel.add(btnProxy);
            }
            panel = chk;
        }
        JButton defaultOption = options[options.length - 1];
        JOptionPane pane = new JOptionPane(
            message, style, JOptionPane.DEFAULT_OPTION, null, new Object[] {panel}
        );
        JDialog dlg = pane.createDialog(parent, title);
        for (JButton option : options) {
            option.addActionListener(e -> {
                pane.setValue(option);
                dlg.dispose();
            });
        }
        dlg.getRootPane().setDefaultButton(defaultOption);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                defaultOption.requestFocus();
            }
        });
        dlg.pack();
        dlg.setLocationRelativeTo(null);
        dlg.setAlwaysOnTop(true);
        dlg.setVisible(true);
        if (check != null) {
            check.setDoNotShow(cbShow.isSelected());
        }
        if (retValues != null) {
            Object value = pane.getValue();
            if (value != null) {
                for (int i = 0; i < options.length; i++) {
                    if (value.equals(options[i])) {
                        return retValues[i];
                    }
                }
            }
        }
        return Result.ABORT;
    }

    private void setLnF() {
        if (!lnfSet) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // ignore
            }
            lnfSet = true;
        }
    }

    private void doShowError(Component parent, String message) {
        showDialogInternal(
            parent, message, ERROR, JOptionPane.ERROR_MESSAGE, new String[] {"OK"}, null,
            null, null
        );
    }

    public void showError(String message) {
        doShowError(null, message);
    }

    public void showWarning(String message) {
        showDialogInternal(
            null, message,
            WARNING, JOptionPane.WARNING_MESSAGE, new String[] {"OK"}, null,
            null, null
        );
    }

    public void showSuccess(String message) {
        showDialogInternal(
            null, message,
            "Успешное завершение", JOptionPane.INFORMATION_MESSAGE, new String[] {"OK"}, null,
            null, null
        );
    }

    public Result showError2(String message, FileLoader loader) {
        return showDialogInternal(
            null, message,
            ERROR, JOptionPane.ERROR_MESSAGE,
            new String[] {"Отмена", "Повторить"}, new Result[] {Result.ABORT, Result.RETRY},
            null, loader
        );
    }

    public Result showWarning2(String message, FileLoader loader) {
        return showDialogInternal(
            null, message,
            WARNING, JOptionPane.WARNING_MESSAGE,
            new String[] {"Продолжить", "Отмена"}, new Result[] {Result.IGNORE, Result.ABORT},
            loader, null
        );
    }

    public Result showWarning3(String message, FileLoader loader) {
        return showDialogInternal(
            null, message,
            WARNING, JOptionPane.WARNING_MESSAGE,
            new String[] {"Пропустить", "Отмена", "Повторить"}, new Result[] {Result.IGNORE, Result.ABORT, Result.RETRY},
            loader, loader
        );
    }

    void showProxyDialog(Component owner, ProxyConfig proxy, URL url, FileLoader loader) {
        setLnF();
        ProxyDialog pdlg = new ProxyDialog(owner, proxy, url, this::logError, this::doShowError);
        ProxyConfig newProxy = pdlg.getProxy();
        if (newProxy != null && loader != null) {
            loader.setProxy(newProxy);
        }
    }

    public void showProxyDialog(ProxyConfig proxy, URL url, FileLoader loader) {
        showProxyDialog(null, proxy, url, loader);
    }

    public URL askUrl(HttpInteraction http) {
        setLnF();
        UrlDialog udlg = new UrlDialog(null, http, this::logError, this::doShowError);
        return udlg.getURL();
    }

    public ClientProfile chooseProfile(List<ClientProfile> profiles) {
        setLnF();
        ProfileDialog dlg = new ProfileDialog(null, profiles);
        return dlg.getProfile();
    }

    public IUpdateProgress clientUpdateProgress(boolean hasProfileChoice) {
        return new UIProgress(hasProfileChoice);
    }

    private static final class ProgressWindow extends JFrame {

        final JProgressBar bar = new JProgressBar(0, 100);

        ProgressWindow() {
            super("Обновление приложения");

            bar.setStringPainted(true);
            bar.setPreferredSize(new Dimension(300, bar.getPreferredSize().height));

            JPanel main = new JPanel(new BorderLayout());
            main.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            main.add(new JLabel("Обновление приложения:"), BorderLayout.NORTH);
            main.add(bar, BorderLayout.CENTER);
            add(main, BorderLayout.CENTER);

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    private static final class UIProgress implements IUpdateProgress {

        private final boolean useWindow;
        private ProgressWindow window = null;

        UIProgress(boolean useWindow) {
            this.useWindow = useWindow;
        }

        public void setPercent(int percent) {
            SwingUtilities.invokeLater(() -> {
                if (useWindow) {
                    if (window == null) {
                        window = new ProgressWindow();
                    }
                    window.bar.setValue(percent);
                    window.bar.setString(percent + "%");
                } else {
                    SplashStatus.setStatus("Обновление приложения: " + percent + "%");
                }
            });
        }

        public void close() {
            SwingUtilities.invokeLater(() -> {
                if (window != null) {
                    window.dispose();
                    window = null;
                }
            });
        }
    }

    static ILoaderGui create() {
        return GraphicsEnvironment.isHeadless() ? new HeadlessGui() : new LoaderGui();
    }
}
