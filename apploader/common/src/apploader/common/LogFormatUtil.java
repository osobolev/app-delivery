package apploader.common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

public final class LogFormatUtil {

    private static final ThreadLocal<DateFormat> TIMESTAMP_FORMAT = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            return DateFormat.getDateTimeInstance();
        }
    };

    public static String getTimestamp() {
        return TIMESTAMP_FORMAT.get().format(new Date());
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintWriter open(String fileName) {
        try {
            PrintWriter fileOutput = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)), true);
            start(fileOutput);
            return fileOutput;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintWriter getWriter(PrintWriter fileOutput) {
        return fileOutput == null ? new PrintWriter(System.out, true) : fileOutput;
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
