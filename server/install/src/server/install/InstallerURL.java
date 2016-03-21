package server.install;

import apploader.common.ConfigReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
        OutputStream os = new FileOutputStream(dest);
        try {
            String urlProp = ConfigReader.HTTP_SERVER_PROPERTY + "=" + url + "\n";
            os.write(urlProp.getBytes(ConfigReader.CHARSET));
        } finally {
            IOUtils.close(os);
        }
    }
}
