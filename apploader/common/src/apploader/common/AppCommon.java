package apploader.common;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

public final class AppCommon {

    /**
     * System property containing name of the application to run
     */
    public static final String APPLICATION_PROPERTY = "application";
    /**
     * Name of the server resource containing list of all available server applications
     */
    public static final String GLOBAL_APP_LIST = "global_app.list";
    public static final String HTTPS_CERT = "https.crt";

    public static String getSplashName(String application) {
        return application + "_splash.jpg";
    }

    public static void generateNativeFile(File file, boolean windows, Consumer<PrintWriter> consumer) throws IOException {
        String encoding = windows ? "Cp1251" : "UTF-8";
        String systemLineSeparator = windows ? "\r\n" : "\n";
        try (PrintWriter pw = new PrintWriter(file, encoding) {
            @Override
            public void println() {
                try {
                    out.write(systemLineSeparator);
                } catch (IOException x) {
                    setError();
                }
            }
        }) {
            consumer.accept(pw);
        }
        if (!windows) {
            file.setExecutable(true);
        }
    }

    public static void generateBatFile(File file, String app) throws IOException {
        generateNativeFile(file, true, pw -> {
            pw.println("@echo off");
            pw.println("call checknew.bat");
            pw.println("call setjava.bat");
            String splash = " -splash:" + getSplashName(app);
            pw.println("%JAVABIN% -D" + APPLICATION_PROPERTY + "=" + app + splash + " -jar apploader.jar %*");
        });
    }

    public static void generateShellFile(File file, String app) throws IOException {
        generateNativeFile(file, false, pw -> {
            pw.println("#!/usr/bin/env sh");
            pw.println(". ./checknew.sh");
            pw.println(". ./setjava.sh");
            String splash = " -splash:" + getSplashName(app);
            pw.println("$JAVABIN -D" + APPLICATION_PROPERTY + "=" + app + splash + " -jar apploader.jar \"$@\"");
        });
    }

    public static URL resolve(URL base, String file) throws MalformedURLException {
        try {
            return base.toURI().resolve(file).toURL();
        } catch (URISyntaxException ex) {
            MalformedURLException mex = new MalformedURLException(ex.getMessage());
            mex.initCause(ex);
            throw mex;
        }
    }

    public static String getRemotingContext(String application) {
        return application + "/remoting";
    }

    public static URL getRemotingUrl(URL serverUrl, String application) throws MalformedURLException {
        return resolve(serverUrl, getRemotingContext(application));
    }

    private static boolean detectWindows() {
        try {
            String os = System.getProperty("os.name");
            if (os != null) {
                return os.toLowerCase().startsWith("windows");
            }
        } catch (Exception ex) {
            // ignore
        }
        return true;
    }

    private static final boolean IS_WINDOWS = detectWindows();

    public static boolean isWindows() {
        return IS_WINDOWS;
    }
}
