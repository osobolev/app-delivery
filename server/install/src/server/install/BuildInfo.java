package server.install;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final Properties profileProps;

    public BuildInfo(Consumer<String> logger, File root, File buildDir, Properties profileProps) {
        this.logger = logger;
        this.root = root;
        this.buildDir = buildDir;
        this.profileProps = profileProps;
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

    private static List<File> dirsToFiles(List<String> tryDirs, String... exeNames) {
        return tryDirs
            .stream()
            .flatMap(path -> Stream.of(exeNames).map(exeName -> new File(path, exeName)))
            .collect(Collectors.toList());
    }

    public File findWindowsExe(String dirProp, String exeFolder, String... exeNames) {
        List<String> tryDirs = new ArrayList<>(3);
        String dirValue = profileProps.getProperty(dirProp);
        if (dirValue != null) {
            tryDirs.add(dirValue);
        }
        String pf = System.getenv("ProgramFiles");
        if (pf != null) {
            tryDirs.add(pf + File.separator + exeFolder);
        }
        String pf32 = System.getenv("ProgramFiles(x86)");
        if (pf32 != null) {
            tryDirs.add(pf32 + File.separator + exeFolder);
        }
        List<File> tryFiles = dirsToFiles(tryDirs, exeNames);
        return findExe(tryFiles);
    }

    public File findLinuxExe(String dirProp, String fileProp, String... exeNames) {
        List<String> tryDirs = new ArrayList<>(4);
        String dirValue = dirProp == null ? null : profileProps.getProperty(dirProp);
        if (dirValue != null) {
            tryDirs.add(dirValue);
        }
        tryDirs.add("/bin");
        tryDirs.add("/usr/bin");
        tryDirs.add("/usr/local/bin");
        List<File> tryFiles = new ArrayList<>(dirsToFiles(tryDirs, exeNames));
        String fileValue = fileProp == null ? null : profileProps.getProperty(fileProp);
        if (fileValue != null) {
            tryFiles.add(new File(fileValue));
        }
        return findExe(tryFiles);
    }

    public String getProperty(String name, String defValue) {
        return profileProps.getProperty(name, defValue);
    }

    public void log(String message) {
        logger.accept(message);
    }
}
