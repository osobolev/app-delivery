package server.install;

import apploader.common.AppCommon;

import java.io.File;
import java.io.IOException;

final class ClientResource extends InstallerResource {

    private final String app;
    private final boolean windows;

    ClientResource(String destName, String app, boolean windows) {
        super(destName);
        this.app = app;
        this.windows = windows;
    }

    boolean isModifiedAfter(long ts) {
        return false;
    }

    void checkExists() {
    }

    void copyTo(File dest) throws IOException {
        if (windows) {
            AppCommon.generateBatFile(dest, app);
        } else {
            AppCommon.generateShellFile(dest, app);
        }
    }
}
