package server.install;

import apploader.common.ClientBat;

import java.io.File;
import java.io.IOException;

final class ClientResource extends InstallerResource {

    private final String app;

    ClientResource(String destName, String app) {
        super(destName);
        this.app = app;
    }

    boolean isModifiedAfter(long ts) {
        return false;
    }

    void checkExists() {
    }

    void copyTo(File dest) throws IOException {
        ClientBat.generateBatFile(dest, app);
    }
}
