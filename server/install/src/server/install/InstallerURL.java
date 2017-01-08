package server.install;

import apploader.common.ConfigReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

final class InstallerURL extends InstallerResource {

    private final String url;

    InstallerURL(String destName, String url) {
        super(destName);
        this.url = url;
    }

    boolean isModifiedAfter(long ts) {
        return false;
    }

    void checkExists() {
    }

    void copyTo(File dest) throws IOException {
        PrintWriter pw = new PrintWriter(dest, ConfigReader.CHARSET);
        pw.println(ConfigReader.HTTP_SERVER_PROPERTY + "=" + url);
        pw.close();
    }
}
