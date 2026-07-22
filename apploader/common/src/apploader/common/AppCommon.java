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
    /**
     * Name of the server resource containing list of all available client profiles
     */
    public static final String PROFILE_LIST = "profile.list";
    /**
     * Name of the server resource containing the major version of client (changing it requires full reinstall)
     */
    public static final String MAJOR_VERSION = "major.version";
    /**
     * Profile list query parameter to filter profiles by OS
     */
    public static final String PROFILE_WINDOWS = "windows";
    /**
     * Profile list query parameter to filter profiles by bitness
     */
    public static final String PROFILE_BITS = "bits";
    public static final String HTTPS_CERT = "https.crt";

    public static String getSplashName(String application) {
        return application + "_splash.jpg";
    }

    public static void generateNativeFile(File file, boolean windows, Consumer<PrintWriter> consumer) throws IOException {
        String encoding = windows ? "Cp1251" : "UTF-8";
        String systemLineSeparator = windows ? "\r\n" : "\n";
        try (PrintWriter pw = new PrintWriter(file, encoding) {
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

    public static int currentJavaBits() {
        String model = System.getProperty("sun.arch.data.model");
        if (model != null) {
            if ("64".equals(model)) {
                return 64;
            } else if ("32".equals(model)) {
                return 32;
            }
        }
        String arch = System.getProperty("os.arch");
        if (arch != null) {
            if (arch.endsWith("64")) {
                return 64;
            } else if ("x86".equals(arch) || arch.matches("i\\d86")) {
                return 32;
            }
        }
        return 0;
    }

    public static int getOSBits() {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        if (wow64Arch != null) {
            return wow64Arch.endsWith("64") ? 64 : 32;
        } else if (arch != null) {
            return arch.endsWith("64") ? 64 : 32;
        } else {
            return currentJavaBits();
        }
    }
}
