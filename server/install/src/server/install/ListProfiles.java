package server.install;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class ListProfiles {

    public static Map<String, String> listProfiles(File rootDir, Boolean windowsClient, InstallLogger logger) {
        Map<String, String> profiles = new LinkedHashMap<>();
        rootDir.listFiles((dir, fileName) -> {
            String profileName = Profile.getFileProfile(fileName);
            if (profileName == null)
                return false;
            Profile profile = Profile.create(profileName);
            Properties profileProps = profile.loadProfileProps(logger, dir);
            if (windowsClient != null) {
                if (profile.isWindows(profileProps) != windowsClient.booleanValue())
                    return false;
            }
            String profileDescription = profileProps.getProperty("name", profileName);
            profiles.put(profileName, profileDescription);
            return true;
        });
        return profiles;
    }
}
