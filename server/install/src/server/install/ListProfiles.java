package server.install;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ListProfiles {

    public static List<String> listProfiles(File rootDir, Boolean windowsClient, InstallLogger logger) {
        List<String> profiles = new ArrayList<>();
        rootDir.listFiles((dir, fileName) -> {
            String profileName = Profile.getFileProfile(fileName);
            if (profileName == null)
                return false;
            if (windowsClient != null) {
                Profile profile = Profile.create(profileName);
                Properties profileProps = profile.loadProfileProps(logger, dir);
                if (profile.isWindows(profileProps) != windowsClient.booleanValue())
                    return false;
            }
            profiles.add(profileName);
            return true;
        });
        return profiles;
    }
}
