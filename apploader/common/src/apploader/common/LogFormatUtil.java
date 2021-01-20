package apploader.common;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class LogFormatUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private static final Charset LOG_CHARSET = Charset.defaultCharset();

    public static String getTimestamp() {
        return TIMESTAMP_FORMAT.format(LocalDateTime.now());
    }

    private static PrintWriter pw(OutputStream os) {
        return new PrintWriter(new OutputStreamWriter(os, LOG_CHARSET), true);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintWriter open(String fileName) {
        try {
            PrintWriter fileOutput = pw(new BufferedOutputStream(new FileOutputStream(fileName, true)));
            start(fileOutput);
            return fileOutput;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintWriter getWriter(PrintWriter fileOutput) {
        return fileOutput == null ? pw(System.out) : fileOutput;
    }

    public static void start(PrintWriter pw) {
        String date = getTimestamp();
        pw.println("------------------ Log started: " + date + " ------------------");
    }

    public static void printHeader(PrintWriter pw, String type) {
        printMessage(pw, type, null);
    }

    public static void printMessage(PrintWriter pw, String type, String message) {
        pw.println("[" + type + "] " + getTimestamp() + (message == null ? "" : " | " + message));
    }

    public static void output(PrintWriter pw, String type, String line) {
        printHeader(pw, type);
        pw.println(line);
    }

    public static void printStackTrace(PrintWriter pw, Throwable error) {
        printMessage(pw, "ERROR", error.toString());
        error.printStackTrace(pw);
        pw.println("-------------------------------");
    }
}
