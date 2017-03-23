package server.desktop;

import javax.swing.*;

final class SyncBox {

    private boolean state;
    private final JCheckBox cb;

    SyncBox(JCheckBox cb) {
        this.cb = cb;
        this.state = cb.isSelected();
        cb.addActionListener(e -> {
            synchronized (cb) {
                state = cb.isSelected();
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
