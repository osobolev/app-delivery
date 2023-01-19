package server.install;

import apploader.common.AppCommon;

import java.io.File;
import java.io.IOException;

/**
 * Генерация setjava.bat/setjava.sh
 */
final class SetJavaResource extends InstallerResource {

    private final boolean windows;

    SetJavaResource(String destName, boolean windows) {
        super(destName);
        this.windows = windows;
    }

    boolean isModifiedAfter(long ts) {
        return false;
    }

    void checkExists() {
    }

    void copyTo(File dest) throws IOException {
        AppCommon.generateNativeFile(dest, windows, pw -> {
            if (windows) {
                pw.println("set JAVABIN=start jre\\bin\\javaw.exe");
            } else {
                pw.println("export JAVABIN=./jre/bin/java");
            }
        });
    }
}
