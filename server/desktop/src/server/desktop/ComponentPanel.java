package server.desktop;

import server.jetty.AppServerComponent;
import sqlg2.db.SimpleLogger;
import sqlg2.db.SqlTrace;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

final class ComponentPanel extends JPanel {

    private final JLabel lbl;
    private final JCheckBox cbControl = new JCheckBox("Остановить/запустить");
    private final JLabel lblControl;
    private final SyncBox cbTrace = new SyncBox(new JCheckBox("Все сообщения", true));
    private final SyncBox cbSqlTrace = new SyncBox(new JCheckBox("Выводить SQL", false));
    private final JTextArea taLog = new JTextArea(15, 80);

    private final AppServerComponent comp;

    ComponentPanel(final AppServerComponent comp, JLabel lblControl) {
        super(new BorderLayout());
        this.comp = comp;
        this.lblControl = lblControl;

        taLog.setEditable(false);
        lbl = new JLabel("Сервер '" + comp.getName() + "'", JLabel.LEFT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        JPanel mode = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        mode.add(new JLabel("Вывод:"));
        mode.add(cbTrace.getVisual());
        mode.add(cbSqlTrace.getVisual());

        JPanel up = new JPanel(new BorderLayout());
        up.add(lbl, BorderLayout.WEST);
        up.add(mode, BorderLayout.EAST);
        add(up, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(taLog);
        scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.darkGray));
        add(scrollPane, BorderLayout.CENTER);

        add(cbControl, BorderLayout.SOUTH);

        cbControl.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (comp.isRunning()) {
                    stop();
                } else {
                    start();
                }
            }
        });

        setButtonState();
    }

    void start() {
        comp.start();
        setButtonState();
    }

    private void stop() {
        comp.stop();
        setButtonState();
    }

    private void setButtonState() {
        boolean running = comp.isRunning();
        lbl.setEnabled(running);
        cbControl.setSelected(running);
        lblControl.setForeground(running ? null : Color.red);
    }

    SimpleLogger getLogger() {
        return new TextLogger(comp.application);
    }

    SqlTrace getTrace() {
        return new SqlTrace() {
            public String getTraceMessage(boolean ok, long time) {
                return cbSqlTrace.isSelected() ? "\nSQL trace" : null;
            }
        };
    }

    private final class TextLogger extends SimpleLogger {

        TextLogger(String application) {
            super(application + "_server_errors.log");
        }

        private void append(final String str) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    taLog.append(str + "\n");
                    taLog.setCaretPosition(taLog.getDocument().getLength());
                }
            });
        }

        public void info(String message) {
            super.info(message);
            if (cbTrace.isSelected()) {
                append(message);
            }
        }

        public void error(String message) {
            super.error(message);
            append(message);
        }

        public void error(Throwable error) {
            super.error(error);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            error.printStackTrace(pw);
            pw.close();
            append(sw.toString());
        }
    }
}
