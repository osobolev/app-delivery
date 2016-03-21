package server.install;

import java.io.File;
import java.io.IOException;

final class InstallerFile extends InstallerResource {

    private final File src;

    InstallerFile(File src, String destName) {
        super(destName);
        this.src = src;
    }

    boolean isModifiedAfter(long ts) {
        return src.lastModified() > ts;
    }

    void checkExists() throws BuildException {
        if (!src.exists())
            throw new BuildException("���� '" + src.getName() + "' �� ����������");
    }

    void copyTo(File dest) throws IOException {
        IOUtils.copyFile(dest, src);
    }
}
