package server.jetty;

import apploader.common.LogFormatUtil;
import sqlg2.db.SQLGLogger;

import java.io.PrintWriter;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
final class JettyLogger implements SQLGLogger {

    private final PrintWriter pw;
    private final PrintWriter fileOutput;

    JettyLogger(String fileName) {
        this.fileOutput = LogFormatUtil.open(fileName);
        this.pw = LogFormatUtil.getWriter(fileOutput);
    }

    public void trace(String message) {
        System.out.println(message);
    }

    public void info(String message) {
        LogFormatUtil.output(pw, "INFO", message);
    }

    public void error(String message) {
        LogFormatUtil.output(pw, "ERROR", message);
    }

    public void error(Throwable error) {
        LogFormatUtil.printStackTrace(pw, error);
    }

    public void close() {
        if (fileOutput != null) {
            fileOutput.close();
        }
        LogFormatUtil.shutdown();
    }
}
