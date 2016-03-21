package server.install;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class InstallBuilder {

    private final File baseDir;
    private final File buildDir;
    private final List<InstallerResource> depends;

    private final File root;
    private final AtomicInteger percentCell = new AtomicInteger();

    public InstallBuilder(SourceFiles src) {
        this.root = src.root;
        this.baseDir = src.baseDir;
        this.buildDir = new File(baseDir, "install");
        this.depends = src.depends;
    }

    private File getExeInstaller() {
        return new File(baseDir, "install.exe");
    }

    private File getZipInstaller() {
        return new File(baseDir, "install.zip");
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

    private void setPercent(int newPercent) {
        int oldPercent = percentCell.get();
        if (newPercent > oldPercent) {
            int percent = Math.min(newPercent, 100);
            percentCell.set(percent);
            System.out.println(percent + "%");
        }
    }

    void workPercent(double... values) {
        double realPercent = 0;
        double denom = 1;
        for (int i = 0; i < values.length; i += 2) {
            double current = values[i];
            double total = values[i + 1];
            double percent = current / total;
            realPercent += percent / denom;
            denom *= total;
        }
        int newPercent = (int) Math.round(realPercent * 100);
        setPercent(newPercent);
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
        setPercent(50);
        return countFiles;
    }

    private static File findExe(Properties installProps, String propName, String exeFolder, String exeName) {
        List<String> tryPaths = new ArrayList<String>(3);
        String propValue = installProps == null ? null : installProps.getProperty(propName);
        if (propValue != null) {
            tryPaths.add(propValue);
        }
        String pf = System.getenv("ProgramFiles");
        if (pf != null) {
            tryPaths.add(pf + File.separator + exeFolder);
        }
        String pf32 = System.getenv("ProgramFiles(x86)");
        if (pf32 != null) {
            tryPaths.add(pf32 + File.separator + exeFolder);
        }
        for (String tryPath : tryPaths) {
            File exe = new File(tryPath, exeName);
            if (exe.canExecute())
                return exe;
        }
        return null;
    }

    private File createRarInstaller(int countFiles, Properties installProps) throws IOException {
        File winRar = findExe(installProps, "rar.dir", "WinRAR", "rar.exe");
        if (winRar == null)
            return null;

        String sfxConfig = "sfx.cfg";
        File sfx = new File(root, sfxConfig); // todo: internal info
        if (sfx.isFile()) {
            IOUtils.copyFile(new File(baseDir, sfxConfig), sfx);
        } else {
            return null;
        }

        File exe = getExeInstaller();
        String[] args = {
            winRar.getAbsolutePath(),
            "a",    // add
            "-ep1", // exclude base directory from names
            "-z" + sfxConfig,
            "-sfx", // create SFX
            "-r",   // recurse subdirs
            "-m1",  // compression level to fast
            "-idp", // do not output percents
            exe.getAbsolutePath(),
            buildDir.getAbsolutePath() + "\\*"
        };
        Process process = Runtime.getRuntime().exec(args, null, baseDir);
        new Thread(new CountOutputEater(process.getInputStream(), countFiles + 5, this)).start();
        new Thread(new OutputEater(process.getErrorStream())).start();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        setPercent(100);
        return exe;
    }

    private File createInnoInstaller(int countFiles, Properties installProps) throws IOException {
        File iscc = findExe(installProps, "inno.dir", "Inno Setup 5", "ISCC.exe");
        if (iscc == null)
            return null;

        String issScript = "client.iss";
        File iss = new File(root, issScript); // todo: internal info
        File issCommon = new File(root, "common.iss"); // todo: internal info
        if (iss.isFile() && issCommon.isFile()) {
            IOUtils.copyTo(buildDir, iss);
            IOUtils.copyTo(buildDir, issCommon);
        } else {
            return null;
        }

        String[] args = {
            iscc.getAbsolutePath(),
            issScript
        };
        Process process = Runtime.getRuntime().exec(args, null, buildDir);
        new Thread(new CountOutputEater(process.getInputStream(), countFiles * 2 + 70, this)).start();
        new Thread(new OutputEater(process.getErrorStream())).start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0)
                return null;
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        setPercent(100);
        return getExeInstaller();
    }

    private File createZipInstaller(int countFiles) throws IOException {
        File zip = getZipInstaller();
        OutputStream os = new FileOutputStream(zip);
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            os = zos;
            addToZip(zos, buildDir, null, new int[1], countFiles);
        } finally {
            IOUtils.close(os);
        }
        setPercent(100);
        return zip;
    }

    private void addToZip(ZipOutputStream zip, File root, String path, int[] count, int total) throws IOException {
        File dir;
        if (path == null) {
            dir = root;
        } else {
            dir = new File(root, path);
        }
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (File file : files) {
            String nextPath;
            if (path == null) {
                nextPath = file.getName();
            } else {
                nextPath = path + "/" + file.getName();
            }
            if (file.isDirectory()) {
                addToZip(zip, root, nextPath, count, total);
            } else if (file.isFile()) {
                zip.putNextEntry(new ZipEntry(nextPath));
                IOUtils.copyFile(zip, file);
                zip.closeEntry();
            }
            workPercent(1, 2, count[0]++, total);
        }
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
                workPercent(0, 2, counter[0]++, total);
                IOUtils.copyFile(copy, file);
            }
        }
    }

    private static int countFiles(File root, File exclude) throws IOException, BuildException {
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

    public File getReadyInstaller() {
        File exe = getExeInstaller();
        if (!isModified(exe))
            return exe;
        File zip = getZipInstaller();
        if (!isModified(zip))
            return zip;
        return null;
    }

    private Properties loadInstallProps() throws IOException {
        File properties = new File(root, "install.properties"); // todo: internal info
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
        // todo: check depends here???
        int countFiles = buildInstaller(installProps);
        File innoExe = createInnoInstaller(countFiles, installProps);
        if (innoExe != null)
            return innoExe;
        File rarExe = createRarInstaller(countFiles, installProps);
        if (rarExe != null)
            return rarExe;
        return createZipInstaller(countFiles);
    }

    public void resetPercent() {
        percentCell.set(0);
    }

    public int getPercent() {
        return percentCell.get();
    }
}
