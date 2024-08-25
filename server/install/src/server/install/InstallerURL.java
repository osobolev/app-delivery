package server.install;

import apploader.common.ConfigReader;

import java.io.File;
import java.io.IOException;

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
        ConfigReader.writeAppProperties(dest, url);
    }
}
