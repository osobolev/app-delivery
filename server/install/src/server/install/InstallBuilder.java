package server.install;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public final class InstallBuilder {

    private final File root;     // ./
    private final File baseDir;  // ./client или ./client/profile
    private final File buildDir; // ./client/install или ./client/profile/install
    private final List<InstallerResource> depends;
    private final JavaSource javaSource;
    private final List<Packer> packers;

    private final List<String> apps;
    private final Properties profileProps;
    private final Consumer<String> logger;
    private final PercentCell percentCell;

    private InstallBuilder(SourceFiles src, List<String> apps, Properties profileProps, Consumer<String> logger) {
        this.root = src.root;
        this.baseDir = src.baseDir;
        this.buildDir = new File(baseDir, "install");
        this.depends = src.depends;
        this.javaSource = src.javaSource;
        this.packers = src.packers;

        this.apps = apps;
        this.profileProps = profileProps;
        this.logger = logger;
        this.percentCell = new PercentCell(logger);
    }

    public static InstallBuilder create(File root, List<String> apps, String profileStr, String url, Consumer<String> logger) {
        Profile profile = Profile.create(profileStr);
        Properties profileProps = profile.loadProfileProps(root);
        SourceFiles src = new SourceFiles(root, apps, profile, url, profileProps);
        return new InstallBuilder(src, apps, profileProps, logger);
    }

    private static void clean(File dir) {
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (File file : files) {
            if (file.isDirectory()) {
                clean(file);
            }
            file.delete();
        }
    }

    private int buildInstaller() throws IOException, BuildException {
        clean(buildDir);
        File javaDest = new File(buildDir, "jre");
        int countFiles = javaSource.copyJava(percentCell, javaDest);
        for (InstallerResource depend : depends) {
            countFiles++;
            File destFile = depend.getDestFile(buildDir);
            depend.checkExists();
            depend.copyTo(destFile);
        }
        percentCell.setPercent(50);
        return countFiles;
    }

    private File getResultFile(Packer packer) {
        String installerBaseName = profileProps.getProperty("base.name", "install");
        return new File(baseDir, packer.getResultFileName(installerBaseName));
    }

    private boolean isModified(File installer) {
        long lm = installer.lastModified();
        if (lm <= 0)
            return true;
        for (InstallerResource depend : depends) {
            if (depend.isModifiedAfter(lm))
                return true;
        }
        return false;
    }

    public File getReadyInstaller() {
        for (Packer packer : packers) {
            File result = getResultFile(packer);
            if (!isModified(result))
                return result;
        }
        return null;
    }

    public File getInstaller() throws IOException, BuildException {
        File ready = getReadyInstaller();
        if (ready != null)
            return ready;
        int countFiles = buildInstaller();
        BuildInfo info = new BuildInfo(logger, root, buildDir, apps, profileProps);
        for (Packer packer : packers) {
            File result = getResultFile(packer);
            logger.accept("Trying packer " + packer.getClass().getSimpleName());
            if (packer.buildResultFile(info, percentCell, countFiles, result)) {
                logger.accept("Packer success");
                percentCell.setPercent(100);
                return result;
            }
        }
        throw new BuildException("All packers failed!");
    }

    public PercentCell getPercentCell() {
        return percentCell;
    }
}
