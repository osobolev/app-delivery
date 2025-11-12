package apploader;

import apploader.common.AppCommon;
import apploader.lib.FileResult;
import apploader.lib.IFileLoader;
import apploader.lib.ILoaderGui;
import apploader.lib.Result;

import java.io.File;

final class TZUpdater {

    private FileResult newTZUpdater = null;
    private FileResult newTimeZones = null;

    void add(String right, FileResult fileResult) {
        if ("tzupdater.jar".equals(right)) {
            newTZUpdater = fileResult;
        } else if ("tzdata.tar.gz".equals(right)) {
            newTimeZones = fileResult;
        }
    }

    boolean update(ILoaderGui gui, IFileLoader fileLoader) {
        if (newTZUpdater != null && newTimeZones != null) {
            boolean anyUpdated = newTZUpdater.updated || newTimeZones.updated;
            Boolean updateResult = updateTimeZones(gui, fileLoader, anyUpdated, newTimeZones.file);
            if (updateResult == null)
                return false;
            if (updateResult.booleanValue()) {
                gui.showWarning("Обновлены данные часовых поясов, перезапустите приложение");
                return false;
            }
        }
        return true;
    }

    /**
     * @return
     *   false, если не нужно обновлять TZDB;
     *   true, если TZDB обновлена и нужен перезапуск;
     *   null, если ошибка при обновлении.
     */
    private static Boolean updateTimeZones(ILoaderGui gui, IFileLoader fileLoader, boolean newFiles, File dataFile) {
        File successFile = fileLoader.getLocalFile("tzupdater.done");
        if (successFile.exists() && !newFiles)
            return false;
        File javaHome = JavaUpdater.getLocalJRE(fileLoader);
        if (javaHome == null) {
            // Our java is not in "jre" folder, cannot update it
            return false;
        }
        boolean success = false;
        try {
            File javaBin = new File(new File(javaHome, "bin"), AppCommon.isWindows() ? "java.exe" : "java");
            ProcessBuilder pb = new ProcessBuilder(
                javaBin.getAbsolutePath(), "-jar", "tzupdater.jar", "-v", "-f", "-l", dataFile.toURI().toString()
            );
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(fileLoader.getLocalFile("tzupdater.log")));
            Process process = pb.start();
            int exitCode = process.waitFor();
            boolean ok;
            if (exitCode == 0) {
                ok = true;
            } else {
                Result ans = gui.showWarning3("Ошибка обновления часовых поясов", null);
                if (ans == Result.ABORT)
                    return null;
                ok = ans == Result.IGNORE;
            }
            if (ok) {
                successFile.createNewFile();
            }
            success = ok;
        } catch (Exception ex) {
            gui.logError(ex);
        }
        if (!success) {
            successFile.delete();
        }
        return true;
    }
}
