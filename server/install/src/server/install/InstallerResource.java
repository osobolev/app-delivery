package server.install;

import java.io.File;
import java.io.IOException;

abstract class InstallerResource {

    private final String destName;

    protected InstallerResource(String destName) {
        this.destName = destName;
    }

    static InstallerResource required(File root, String name) {
        return new InstallerFile(new File(root, name), name);
    }

    static InstallerResource ifExists(File root, String name) {
        File srcFile = new File(root, name);
        if (!srcFile.exists())
            return null;
        return new InstallerFile(srcFile, srcFile.getName());
    }

    static InstallerResource apploader(File root, String srcName, String destName, String url) {
        File srcFile = new File(root, srcName);
        if (srcFile.exists()) {
            return new InstallerFile(srcFile, destName);
        } else {
            return new InstallerURL(destName, url);
        }
    }

    abstract boolean isModifiedAfter(long ts);

    abstract void checkExists() throws BuildException;

    abstract void copyTo(File dest) throws IOException;

    final File getDestFile(File root) {
        return new File(root, destName);
    }
}
