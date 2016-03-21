package server.desktop;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

final class SyncBox {

    private boolean state;
    private final JCheckBox cb;

    SyncBox(final JCheckBox cb) {
        this.cb = cb;
        this.state = cb.isSelected();
        cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synchronized (cb) {
                    state = cb.isSelected();
                }
            }
        });
    }

    boolean isSelected() {
        synchronized (cb) {
            return state;
        }
    }

    JComponent getVisual() {
        return cb;
    }
}
