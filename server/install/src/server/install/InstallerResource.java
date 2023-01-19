package server.install;

import apploader.common.ConfigReader;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;

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

    static InstallerResource osScript(File root, String baseName, boolean windows,
                                      Function<String, InstallerResource> generate) {
        String name = baseName + (windows ? ".bat" : ".sh");
        File srcFile = new File(root, name);
        if (srcFile.exists()) {
            return new InstallerFile(srcFile, name);
        } else {
            return generate.apply(name);
        }
    }

    static InstallerResource apploaderProperties(File root, Profile profile, String url, Properties profileProps) {
        String destName = ConfigReader.APPLOADER_PROPERTIES;
        File srcFile = profile.findPropFile(root, ConfigReader.APPLOADER);
        if (srcFile != null)
            return new InstallerFile(srcFile, destName);
        String propUrl = profileProps.getProperty(ConfigReader.HTTP_SERVER_PROPERTY);
        if (propUrl != null) {
            return new InstallerURL(destName, propUrl);
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
