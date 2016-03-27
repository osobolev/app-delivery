package apploader.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public final class AppCommon {

    public static final String APPLICATION_PROPERTY = "application";

    public static String getSplashName(String application) {
        return application + "_splash.jpg";
    }

    public static void generateBatFile(File file, String app) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(file);
        pw.println("@echo off");
        pw.println("call checknew.bat");
        pw.println("call setjava.bat");
        String splash = " -splash:" + getSplashName(app);
        pw.println("%JAVABIN% -D" + APPLICATION_PROPERTY + "=" + app + splash + " -jar apploader.jar %*");
        pw.close();
    }
}
