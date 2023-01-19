package server.install;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;

import java.io.File;
import java.util.Properties;

final class Profile {

    private final String name;

    private Profile(String name) {
        this.name = name;
    }

    static Profile create(String profile) {
        if (profile == null || profile.isEmpty()) {
            return new Profile(null);
        } else {
            return new Profile(profile);
        }
    }

    private static String getPropFileName(String profileName, String baseName) {
        return profileName == null ? baseName : baseName + "_" + profileName;
    }

    private static File getPropFile(File root, String profileName, String baseName) {
        String fileName = getPropFileName(profileName, baseName) + ConfigReader.PROPERTIES;
        File file = new File(root, fileName);
        if (file.exists())
            return file;
        return null;
    }

    private static Properties loadProps(File root, String profileName) {
        Properties props = new Properties();
        File propFile = getPropFile(root, profileName, "install");
        if (propFile != null) {
            ConfigReader.readProperties(props, propFile);
        }
        return props;
    }

    Properties loadProfileProps(File root) {
        // Loading install.properties:
        Properties installProps = loadProps(root, null);
        if (name != null) {
            // Override install.properties only if there is a  profile
            Properties profileProps = new Properties();
            profileProps.putAll(installProps);
            // Loading install_<profile>.properties:
            Properties props = loadProps(root, name);
            for (String name : props.stringPropertyNames()) {
                String value = props.getProperty(name);
                if (value == null || value.trim().isEmpty()) {
                    profileProps.remove(name);
                } else {
                    profileProps.setProperty(name, value);
                }
            }
            return profileProps;
        } else {
            return installProps;
        }
    }

    boolean isWindows() {
        if (name != null) {
            String lname = name.toLowerCase();
            if (lname.contains(".win")) {
                return true;
            } else if (lname.contains(".lin")) {
                return false;
            }
        }
        return AppCommon.isWindows();
    }

    File getBaseDir(File clientRoot) {
        return name == null ? clientRoot : new File(clientRoot, name);
    }

    File findPropFile(File root, String baseName) {
        File myFile = getPropFile(root, name, baseName);
        if (myFile != null)
            return myFile;
        if (name != null) {
            // Try to find file without "_<profile>" suffix:
            return getPropFile(root, null, baseName);
        } else {
            return null;
        }
    }
}
