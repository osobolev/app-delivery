package server.install;

import apploader.common.AppCommon;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class ListProfiles {

    private static boolean supportsBitness(Properties profileProps, int clientBits) {
        String bits = profileProps.getProperty("java.bits");
        int serverBits;
        if (bits == null) {
            serverBits = AppCommon.getOSBits();
        } else {
            try {
                serverBits = Integer.parseInt(bits);
            } catch (NumberFormatException ex) {
                serverBits = 0;
            }
        }
        return serverBits <= clientBits;
    }

    public static Map<String, String> listProfiles(File rootDir, Boolean windowsClient, int clientBits, InstallLogger logger) {
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
            if (clientBits != 0) {
                if (!supportsBitness(profileProps, clientBits))
                    return false;
            }
            String profileDescription = profileProps.getProperty("name", profileName);
            profiles.put(profileName, profileDescription);
            return true;
        });
        return profiles;
    }
}
