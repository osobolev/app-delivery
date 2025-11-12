package apploader.common;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LogFormatUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static String getTimestamp() {
        return TIMESTAMP_FORMAT.format(LocalDateTime.now());
    }

    public static String getStartMessage() {
        return "Log started: " + getTimestamp();
    }

    private static PrintWriter pw(OutputStream os) {
        return new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true);
    }

    public static PrintWriter openRaw(String fileName) throws IOException {
        return pw(new BufferedOutputStream(new FileOutputStream(fileName, true)));
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintWriter open(String fileName, String startMessage) {
        try {
            PrintWriter fileOutput = openRaw(fileName);
            start(fileOutput, startMessage);
            return fileOutput;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    public static PrintWriter open(String fileName) {
        return open(fileName, null);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintWriter getWriter(PrintWriter fileOutput) {
        return fileOutput == null ? pw(System.out) : fileOutput;
    }

    public static void start(PrintWriter pw, String message) {
        String startMessage = message == null ? getStartMessage() : message;
        pw.println("------------------ " + startMessage + " ------------------");
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
