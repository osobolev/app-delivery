package apploader;

import apploader.lib.ClientProfile;

import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

final class ProfileDialog extends JDialog {

    private final JComboBox<ClientProfile> chProfile;

    private ClientProfile profile = null;

    ProfileDialog(Component owner, List<ClientProfile> profiles) {
        super(owner == null ? null : SwingUtilities.getWindowAncestor(owner), "Обновление приложения", ModalityType.DOCUMENT_MODAL);

        chProfile = new JComboBox<>(profiles.toArray(new ClientProfile[0]));

        JPanel up = new JPanel(new FlowLayout(FlowLayout.LEFT));
        up.add(new JLabel("<html>Приложение будет обновлено.<br>Выберите профиль приложения.</html>"));
        add(up, BorderLayout.NORTH);

        JPanel main = new JPanel(new FlowLayout(FlowLayout.LEFT));
        main.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        main.add(new JLabel("Профиль:"));
        main.add(chProfile);
        add(main, BorderLayout.CENTER);

        JPanel butt = new JPanel(new BasicOptionPaneUI.ButtonAreaLayout(true, 5));
        JButton btnOk = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                int index = chProfile.getSelectedIndex();
                if (index < 0)
                    return;
                profile = profiles.get(index);
                dispose();
            }
        });
        JButton btnCancel = new JButton(new AbstractAction("Отмена") {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        butt.add(btnOk);
        butt.add(btnCancel);
        butt.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        add(butt, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    ClientProfile getProfile() {
        return profile;
    }

    public static void main(String[] args) {
        new ProfileDialog(null, Collections.singletonList(ClientProfile.DEFAULT));
    }
}
