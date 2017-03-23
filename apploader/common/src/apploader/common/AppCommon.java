package apploader.common;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public final class AppCommon {

    /**
     * System property containing name of the application to run
     */
    public static final String APPLICATION_PROPERTY = "application";
    /**
     * Character set of Windows bat files
     */
    public static final String BAT_CHARSET = "Cp1251";
    /**
     * Name of the server resource containing list of all available server applications
     */
    public static final String GLOBAL_APP_LIST = "global_app.list";

    public static String getSplashName(String application) {
        return application + "_splash.jpg";
    }

    public static void generateBatFile(File file, String app) throws IOException {
        try (PrintWriter pw = new PrintWriter(file, BAT_CHARSET)) {
            pw.println("@echo off");
            pw.println("call checknew.bat");
            pw.println("call setjava.bat");
            String splash = " -splash:" + getSplashName(app);
            pw.println("%JAVABIN% -D" + APPLICATION_PROPERTY + "=" + app + splash + " -jar apploader.jar %*");
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void error(Throwable ex) {
        ex.printStackTrace(System.err);
    }
}
