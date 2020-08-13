package server.install;

import apploader.common.AppCommon;
import apploader.common.ConfigReader;
import server.install.packers.InnoPacker;
import server.install.packers.MakeselfPacker;
import server.install.packers.RarPacker;
import server.install.packers.ZipPacker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public final class InstallBuilder {

    private final File root;     // ./
    private final File baseDir;  // ./client или ./client/profile
    private final File buildDir; // ./client/install или ./client/profile/install
    private final List<InstallerResource> depends;

    private final Consumer<String> logger;
    private final PercentCell percentCell;

    private final List<Packer> packers = new ArrayList<>();

    private final Properties installProperties = new Properties();

    public InstallBuilder(SourceFiles src, Consumer<String> logger) {
        this.root = src.root;
        this.baseDir = src.baseDir;
        this.buildDir = new File(baseDir, "install");
        this.depends = src.depends;
        this.logger = logger;
        this.percentCell = new PercentCell(logger);

        if (AppCommon.isWindows()) {
            packers.add(new InnoPacker());
            packers.add(new RarPacker());
        } else {
            packers.add(new MakeselfPacker());
        }
        packers.add(new ZipPacker());
        ConfigReader.readProperties(installProperties, new File(root, "install.properties"));
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

    private static int countFiles(File root, File exclude) throws BuildException {
        File[] files = root.listFiles();
        if (files == null)
            throw new BuildException("JRE не найдена");
        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.equals(exclude)) {
                    count++;
                    count += countFiles(file, exclude);
                }
            } else if (file.isFile()) {
                count++;
            }
        }
        return count;
    }

    private void copy(File to, File from, File exclude, int[] counter, int total) throws IOException {
        to.mkdirs();
        File[] files = from.listFiles();
        if (files == null)
            return;
        for (File file : files) {
            File copy = new File(to, file.getName());
            if (file.isDirectory()) {
                if (!file.equals(exclude)) {
                    copy(copy, file, exclude, counter, total);
                }
            } else if (file.isFile()) {
                percentCell.workPercent(0, 2, counter[0]++, total);
                IOUtils.copyFile(copy, file);
            }
        }
    }

    private int buildInstaller() throws IOException, BuildException {
        clean(buildDir);
        String javaHomePath = installProperties.getProperty("java.dir");
        if (javaHomePath == null) {
            javaHomePath = System.getProperty("java.home");
        }
        File javaHome = new File(javaHomePath);
        File exclude = new File(javaHome, "lib/rt");
        int javaFiles = countFiles(javaHome, exclude);
        copy(new File(buildDir, "jre"), javaHome, exclude, new int[1], javaFiles);
        int countFiles = javaFiles;
        for (InstallerResource depend : depends) {
            countFiles++;
            File destFile = depend.getDestFile(buildDir);
            depend.checkExists();
            depend.copyTo(destFile);
        }
        AppCommon.generateNativeFile(new File(buildDir, "setjava.bat"), true, pw -> pw.println("set JAVABIN=start jre\\bin\\javaw.exe"));
        AppCommon.generateNativeFile(new File(buildDir, "setjava.sh"), false, pw -> pw.println("export JAVABIN=./jre/bin/java"));
        countFiles++;
        percentCell.setPercent(50);
        return countFiles;
    }

    private File getResultFile(Packer packer) {
        String baseName = installProperties.getProperty("base.name", "install");
        return new File(baseDir, packer.getResultFileName(baseName));
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
        BuildInfo info = new BuildInfo(logger, root, buildDir, countFiles, installProperties);
        for (Packer packer : packers) {
            File result = getResultFile(packer);
            logger.accept("Trying packer " + packer.getClass().getSimpleName());
            if (packer.buildResultFile(info, percentCell, result)) {
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
