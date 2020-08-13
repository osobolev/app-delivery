package server.install;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class SourceFiles {

    private static final String APPLOADER_PROPERTIES = ConfigReader.APPLOADER_PROPERTIES;

    final File root;
    final File baseDir;
    final List<InstallerResource> depends = new ArrayList<>();

    public SourceFiles(File root, List<String> apps, String profile, String url) {
        this.root = root;

        boolean noProfile = profile == null || profile.isEmpty();
        File clientRoot = new File(root, "client");
        this.baseDir = noProfile ? clientRoot : new File(clientRoot, profile);

        boolean windows = AppCommon.isWindows();

        depends.add(InstallerResource.required(root, "apploader.jar"));
        if (windows) {
            depends.add(InstallerResource.required(root, "checknew.bat"));
            depends.add(InstallerResource.required(root, "proxy-config.bat"));
            addDependIfExists("install.bat");
        } else {
            depends.add(InstallerResource.required(root, "checknew.sh"));
            depends.add(InstallerResource.required(root, "init-install.sh"));
            depends.add(InstallerResource.required(root, "proxy-config.sh"));
            addDependIfExists("install.sh");
        }
        if (noProfile) {
            depends.add(InstallerResource.apploader(root, APPLOADER_PROPERTIES, APPLOADER_PROPERTIES, url));
        } else {
            depends.add(InstallerResource.apploader(root, "apploader_" + profile + ".properties", APPLOADER_PROPERTIES, url));
        }

        addDependIfExists("proxy.properties");
        for (String app : apps) {
            if (windows) {
                depends.add(InstallerResource.clientScript(root, app + "-client.bat", true, app));
                addDependIfExists(app + ".ico");
                addDependIfExists("uninst_" + app + ".ico");
            } else {
                depends.add(InstallerResource.clientScript(root, app + "-client.sh", false, app));
            }
            addDependIfExists(app + "_splash.jpg");
        }
    }

    private void addDependIfExists(String name) {
        InstallerResource depend = InstallerResource.ifExists(root, name);
        if (depend != null) {
            depends.add(depend);
        }
    }
}
