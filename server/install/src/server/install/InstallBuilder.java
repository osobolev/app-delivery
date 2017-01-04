package server.install;

import server.install.packers.InnoPacker;
import server.install.packers.RarPacker;
import server.install.packers.ZipPacker;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public final class InstallBuilder {

    private final File root;     // ./
    private final File baseDir;  // ./client или ./client/profile
    private final File buildDir; // ./client/install или ./client/profile/install
    private final List<InstallerResource> depends;

    private final PercentCell percentCell = new PercentCell();

    private final List<Packer> packers = Arrays.asList(new InnoPacker(), new RarPacker(), new ZipPacker());

    public InstallBuilder(SourceFiles src) {
        this.root = src.root;
        this.baseDir = src.baseDir;
        this.buildDir = new File(baseDir, "install");
        this.depends = src.depends;
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

    private int buildInstaller(Properties installProps) throws IOException, BuildException {
        clean(buildDir);
        String javaHomePath;
        if (installProps != null) {
            javaHomePath = installProps.getProperty("java.dir");
        } else {
            javaHomePath = null;
        }
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
        PrintWriter pw = new PrintWriter(new File(buildDir, "setjava.bat"));
        pw.println("set JAVABIN=start jre\\bin\\javaw.exe");
        pw.close();
        countFiles++;
        percentCell.setPercent(50);
        return countFiles;
    }

    private File getResultFile(Packer packer) {
        return new File(baseDir, packer.getResultFileName());
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

    private Properties loadInstallProps() throws IOException {
        File properties = new File(root, "install.properties");
        if (!properties.canRead())
            return null;
        Reader rdr = new FileReader(properties);
        Properties props = new Properties();
        try {
            props.load(rdr);
        } finally {
            IOUtils.close(rdr);
        }
        return props;
    }

    public File getInstaller() throws IOException, BuildException {
        File ready = getReadyInstaller();
        if (ready != null)
            return ready;
        Properties installProps = loadInstallProps();
        int countFiles = buildInstaller(installProps);
        BuildInfo info = new BuildInfo(root, buildDir, countFiles, installProps);
        for (Packer packer : packers) {
            File result = getResultFile(packer);
            if (packer.buildResultFile(info, percentCell, result)) {
                percentCell.setPercent(100);
                return result;
            }
        }
        throw new IllegalStateException("All packers failed!");
    }

    public PercentCell getPercentCell() {
        return percentCell;
    }
}
