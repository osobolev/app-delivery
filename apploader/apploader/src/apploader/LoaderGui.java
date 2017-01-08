package apploader;

import apploader.client.ProxyConfig;
import apploader.client.ProxyDialog;

import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

final class LoaderGui {

    private static final String WARNING = "Предупреждение";
    private static final String ERROR = "Ошибка";

    private boolean lnfSet = false;

    private Result showDialogInternal(Component parent,
                                      String message,
                                      String title, int style,
                                      String[] optionNames,
                                      Result[] retValues,
                                      FileLoader check, final FileLoader proxy) {
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
            final JPanel chk = new JPanel(new BorderLayout());
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
        final JButton defaultOption = options[options.length - 1];
        final JOptionPane pane = new JOptionPane(
            message, style, JOptionPane.DEFAULT_OPTION, null, new Object[] {panel}
        );
        final JDialog dlg = pane.createDialog(parent, title);
        for (final JButton option : options) {
            option.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        pane.setValue(option);
                        dlg.dispose();
                    }
                }
            );
        }
        dlg.getRootPane().setDefaultButton(defaultOption);
        dlg.getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                defaultOption.requestFocus();
            }
        });
        dlg.pack();
        dlg.setLocationRelativeTo(null);
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

    void showError(String message) {
        showDialogInternal(
            null, message, ERROR, JOptionPane.ERROR_MESSAGE, new String[] {"OK"}, null,
            null, null
        );
    }

    void showWarning(String message) {
        showDialogInternal(
            null, message,
            WARNING, JOptionPane.WARNING_MESSAGE, new String[] {"OK"}, null,
            null, null
        );
    }

    void showSuccess(String message) {
        showDialogInternal(
            null, message,
            "Успешное завершение", JOptionPane.INFORMATION_MESSAGE, new String[] {"OK"}, null,
            null, null
        );
    }

    Result showError2(String message, FileLoader loader) {
        return showDialogInternal(
            null, message,
            ERROR, JOptionPane.ERROR_MESSAGE,
            new String[] {"Отмена", "Повторить"}, new Result[] {Result.ABORT, Result.RETRY},
            null, loader
        );
    }

    Result showWarning2(String message, FileLoader loader) {
        return showDialogInternal(
            null, message,
            WARNING, JOptionPane.WARNING_MESSAGE,
            new String[] {"Продолжить", "Отмена"}, new Result[] {Result.IGNORE, Result.ABORT},
            loader, null
        );
    }

    Result showWarning3(String message, FileLoader loader) {
        return showDialogInternal(
            null, message,
            WARNING, JOptionPane.WARNING_MESSAGE,
            new String[] {"Пропустить", "Отмена", "Повторить"}, new Result[] {Result.IGNORE, Result.ABORT, Result.RETRY},
            loader, loader
        );
    }

    void showProxyDialog(Component owner, ProxyConfig proxy, URL url, FileLoader loader) {
        setLnF();
        ProxyDialog pdlg = new ProxyDialog(owner, proxy, url, new ProxyDialog.ErrorShow() {
            public void showError(String message) {
                LoaderGui.this.showError(message);
            }
        });
        ProxyConfig newProxy = pdlg.getProxy();
        if (newProxy != null && loader != null) {
            loader.setProxy(newProxy);
        }
    }
}
