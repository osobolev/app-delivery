package server.install;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Определяет, какие файлы нужно копировать в дистрибутив клиента
 */
final class SourceFiles {

    final File root;
    final File baseDir;
    final List<InstallerResource> depends = new ArrayList<>();
    final JavaSource javaSource;
    final List<Packer> packers = new ArrayList<>();

    SourceFiles(File root, List<String> apps, Profile profile, String url, Properties profileProps) {
        this.root = root;
        File clientRoot = new File(root, "client");
        this.baseDir = profile.getBaseDir(clientRoot);

        boolean windowsClient = profile.isWindows(profileProps);

        addRequired("apploader.jar");
        if (windowsClient) {
            addRequired("checknew.bat");
            addRequired("proxy-config.bat");
            addIfExists("install.bat");
        } else {
            addRequired("checknew.sh");
            addRequired("proxy-config.sh");
            addIfExists("install.sh");
        }
        depends.add(InstallerResource.apploaderProperties(root, profile, url, profileProps));
        depends.add(InstallerResource.osScript(
            root, "setjava", windowsClient,
            name -> new SetJavaResource(name, windowsClient)
        ));
        addIfExists("proxy.properties");
        for (String app : apps) {
            depends.add(InstallerResource.osScript(
                root, app + "-client", windowsClient,
                name -> new ClientLauncherResource(name, app, windowsClient)
            ));
            if (windowsClient) {
                addIfExists(app + ".ico");
                addIfExists("uninst_" + app + ".ico");
            }
            addIfExists(app + "_splash.jpg");
        }

        this.javaSource = JavaSource.create(root, profileProps);

        String packerStr = profileProps.getProperty("packers");
        if (packerStr != null) {
            Packer.parsePackers(packers, packerStr, windowsClient);
        } else {
            Packer.addPackers(packers, windowsClient);
        }
    }

    private void addRequired(String name) {
        depends.add(InstallerResource.required(root, name));
    }

    private void addIfExists(String name) {
        InstallerResource depend = InstallerResource.ifExists(root, name);
        if (depend != null) {
            depends.add(depend);
        }
    }
}
