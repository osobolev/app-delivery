package server.embedded;

import apploader.common.LogFormatUtil;
import server.core.AppLogger;

import java.io.PrintWriter;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class EmbeddedLogger implements AppLogger {

    private final PrintWriter pw;
    private final PrintWriter fileOutput;

    public EmbeddedLogger(String fileName) {
        this.fileOutput = LogFormatUtil.open(fileName);
        this.pw = LogFormatUtil.getWriter(fileOutput);
    }

    public EmbeddedLogger() {
        this("appserver.log");
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
    }
}
