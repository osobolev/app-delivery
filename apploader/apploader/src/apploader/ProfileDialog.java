package apploader;

import apploader.lib.ClientProfile;
import apploader.lib.IUpdateProgress;

import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

final class ProfileDialog extends JDialog {

    private static final String BASE_TITLE = "Обновление клиента";
    
    private final ErrorShow error;
    private final BiConsumer<String, IUpdateProgress> action;

    private final JLabel lblUpdate = new JLabel(BASE_TITLE);
    private final JComboBox<ClientProfile> chProfile;
    private final JButton btnOk;
    private final JButton btnCancel;

    private boolean created = false;

    ProfileDialog(Component owner, ErrorShow error, List<ClientProfile> profiles,
                  BiConsumer<String, IUpdateProgress> action) {
        super(owner == null ? null : SwingUtilities.getWindowAncestor(owner), BASE_TITLE, ModalityType.DOCUMENT_MODAL);
        this.error = error;
        this.action = action;

        if (profiles.size() == 1) {
            chProfile = null;
        } else {
            chProfile = new JComboBox<>(profiles.toArray(new ClientProfile[0]));
        }

        JPanel up = new JPanel(new FlowLayout(FlowLayout.LEFT));
        up.add(lblUpdate);
        add(up, BorderLayout.NORTH);

        if (chProfile != null) {
            JPanel main = new JPanel();
            main.add(new JLabel("Профиль:"));
            main.add(chProfile);
            add(main, BorderLayout.CENTER);
        }

        JPanel butt = new JPanel(new BasicOptionPaneUI.ButtonAreaLayout(true, 5));
        btnOk = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                int index = chProfile == null ? 0 : chProfile.getSelectedIndex();
                if (index < 0)
                    return;
                ClientProfile profile = profiles.get(index);
                updateClient(profile.id);
            }
        });
        btnCancel = new JButton(new AbstractAction("Отмена") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        butt.add(btnOk);
        butt.add(btnCancel);
        butt.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        add(butt, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void enableDisable(boolean on) {
        if (chProfile != null) {
            chProfile.setEnabled(on);
        }
        btnOk.setEnabled(on);
        btnCancel.setEnabled(on);
    }

    private void updateClient(String profile) {
        enableDisable(false);
        AtomicReference<String> errorRef = new AtomicReference<>();
        IUpdateProgress progress = new IUpdateProgress() {

            public void setPercent(int percent) {
                lblUpdate.setText(BASE_TITLE + ": " + percent + "%");
            }

            public void done(String error) {
                errorRef.set(error);
            }
        };
        new SwingWorker<Void, Void>() {

            protected Void doInBackground() {
                action.accept(profile, progress);
                return null;
            }

            protected void done() {
                String error = errorRef.get();
                clientCreated(error);
            }
        }.execute();
    }

    private void clientCreated(String errorMessage) {
        enableDisable(true);
        dispose();
        if (errorMessage != null) {
            error.showError(null, errorMessage);
        } else {
            created = true;
        }
    }

    boolean isCreated() {
        return created;
    }
}
