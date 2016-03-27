package server.install;

import apploader.common.ConfigReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class SourceFiles {

    private static final String APPLOADER_PROPERTIES = ConfigReader.APPLOADER_PROPERTIES;

    final File root;
    final File baseDir;
    final List<InstallerResource> depends = new ArrayList<InstallerResource>();

    public SourceFiles(File root, List<String> apps, String profile, String url) {
        this.root = root;

        boolean noProfile = profile == null || profile.isEmpty();
        File clientRoot = new File(root, "client");
        this.baseDir = noProfile ? clientRoot : new File(clientRoot, profile);

        depends.add(InstallerResource.required(root, "apploader.jar"));
        depends.add(InstallerResource.required(root, "checknew.bat"));
        if (noProfile) {
            depends.add(InstallerResource.apploader(root, APPLOADER_PROPERTIES, APPLOADER_PROPERTIES, url));
        } else {
            depends.add(InstallerResource.apploader(root, "apploader_" + profile + ".properties", APPLOADER_PROPERTIES, url));
        }
        depends.add(InstallerResource.required(root, "proxy-config.bat"));

        addDependIfExists("install.bat");
        addDependIfExists("proxy.properties");
        for (String app : apps) {
            depends.add(InstallerResource.clientBat(root, app + "-client.bat", app));
            addDependIfExists(app + "_splash.jpg");
            addDependIfExists(app + ".ico");
            addDependIfExists("uninst_" + app + ".ico");
        }
    }

    private void addDependIfExists(String name) {
        InstallerResource depend = InstallerResource.ifExists(root, name);
        if (depend != null) {
            depends.add(depend);
        }
    }
}
