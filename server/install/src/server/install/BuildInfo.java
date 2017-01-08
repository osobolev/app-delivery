package server.install;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class BuildInfo {

    /**
     * Папка с исходными файлами для инсталлятора (корневая папка веб-приложения)
     */
    public final File root;
    /**
     * Папка, в которую копируются файлы для создания инсталлятора (client/install или client/profile/install)
     */
    public final File buildDir;
    /**
     * Количество файлов в папке buildDir
     */
    public final int countFiles;
    private final Properties installProps;

    public BuildInfo(File root, File buildDir, int countFiles, Properties installProps) {
        this.root = root;
        this.buildDir = buildDir;
        this.countFiles = countFiles;
        this.installProps = installProps;
    }

    public File getSource(String fileName) {
        return new File(root, fileName);
    }

    public File getTarget(File source) {
        return new File(buildDir, source.getName());
    }

    public void copyToTarget(File source) throws IOException {
        IOUtils.copyFile(getTarget(source), source);
    }

    public static File findExe(Properties installProps, String propName, String exeFolder, String exeName) {
        List<String> tryPaths = new ArrayList<String>(3);
        String propValue = installProps.getProperty(propName);
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

    public File findExe(String propName, String exeFolder, String exeName) {
        return findExe(installProps, propName, exeFolder, exeName);
    }
}
