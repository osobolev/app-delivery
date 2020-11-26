package server.desktop;

import server.core.AppLogger;
import server.jetty.AppServerComponent;
import sqlg3.runtime.SqlTrace;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
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

    ComponentPanel(AppServerComponent comp, JLabel lblControl) {
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

        cbControl.addActionListener(e -> {
            if (comp.isRunning()) {
                stop();
            } else {
                start();
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

    LoggerTrace wrap(AppLogger realLogger) {
        TextLogger textLogger = new TextLogger(realLogger);
        SqlTrace trace = (ok, time, messageSupplier) -> {
            if (cbSqlTrace.isSelected()) {
                SqlTrace.doTrace(textLogger::error, "\nSQL trace", messageSupplier);
            }
        };
        return new LoggerTrace(textLogger, trace);
    }

    private final class TextLogger implements AppLogger {

        private final AppLogger logger;

        TextLogger(AppLogger logger) {
            this.logger = logger;
        }

        private void append(String str) {
            SwingUtilities.invokeLater(() -> {
                taLog.append(str + "\n");
                taLog.setCaretPosition(taLog.getDocument().getLength());
            });
        }

        public void trace(String message) {
            logger.trace(message);
        }

        public void info(String message) {
            logger.info(message);
            if (cbTrace.isSelected()) {
                append(message);
            }
        }

        public void error(String message) {
            logger.error(message);
            append(message);
        }

        public void error(Throwable error) {
            logger.error(error);
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                error.printStackTrace(pw);
            }
            append(sw.toString());
        }

        public void close() {
            logger.close();
        }
    }
}
