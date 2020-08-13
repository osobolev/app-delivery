package server.install;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class BuildInfo {

    private final Consumer<String> logger;
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

    public BuildInfo(Consumer<String> logger, File root, File buildDir, int countFiles, Properties installProps) {
        this.logger = logger;
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

    private File findExe(List<File> tryFiles) {
        for (File exe : tryFiles) {
            if (exe.canExecute()) {
                log("Trying '" + exe.getAbsolutePath() + "': SUCCESS");
                return exe;
            } else {
                log("Trying '" + exe.getAbsolutePath() + "': NOT FOUND");
            }
        }
        return null;
    }

    public File findWindowsExe(Properties installProps, String propName, String exeFolder, String exeName) {
        List<String> tryPaths = new ArrayList<>(3);
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
        List<File> tryFiles = tryPaths.stream().map(path -> new File(path, exeName)).collect(Collectors.toList());
        return findExe(tryFiles);
    }

    public File findWindowsExe(String propName, String exeFolder, String exeName) {
        return findWindowsExe(installProps, propName, exeFolder, exeName);
    }

    public File findAnyExe(Properties installProps, String propName, String... exeNames) {
        List<File> tryFiles = new ArrayList<>();
        String propValue = installProps.getProperty(propName);
        if (propValue != null) {
            tryFiles.add(new File(propValue));
        }
        for (String exeName : exeNames) {
            tryFiles.add(new File(exeName));
        }
        return findExe(tryFiles);
    }

    public File findAnyExe(String propName, String... exeNames) {
        return findAnyExe(installProps, propName, exeNames);
    }

    public void log(String message) {
        logger.accept(message);
    }
}
