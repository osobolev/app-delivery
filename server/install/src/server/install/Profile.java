package server.install;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

final class Profile {

    static final Profile NONE = new Profile(null);

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

    private String getPropFileName(String baseName) {
        return name == null ? baseName : baseName + "_" + name;
    }

    private File getPropFile(File root, String baseName) {
        String fileName = getPropFileName(baseName) + ConfigReader.PROPERTIES;
        File file = new File(root, fileName);
        if (file.exists())
            return file;
        return null;
    }

    Properties loadProps(File root) {
        Properties props = new Properties();
        File propFile = getPropFile(root, "install");
        if (propFile != null) {
            ConfigReader.readProperties(props, propFile);
        }
        return props;
    }

    Properties loadProfileProps(File root, Properties installProps) {
        if (name != null) {
            Properties profileProps = new Properties();
            profileProps.putAll(installProps);
            // Override install.properties only if there is a  profile:
            Properties props = loadProps(root);
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

    private List<Profile> priority() {
        if (name == null) {
            return Collections.singletonList(NONE);
        } else {
            return Arrays.asList(this, NONE);
        }
    }

    File findPropFile(File root, String baseName) {
        for (Profile p : priority()) {
            File file = p.getPropFile(root, baseName);
            if (file != null)
                return file;
        }
        return null;
    }
}
