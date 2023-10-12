package apploader;

import apploader.common.AppCommon;
import apploader.lib.IFileLoader;
import apploader.lib.ILoaderGui;

import java.io.File;
import java.io.IOException;
import java.util.List;

final class AppProperties {

    final List<File> jarList;
    final List<File> dllList;
    final String mainClass;

    AppProperties(List<File> jarList, List<File> dllList, String mainClass) {
        this.jarList = jarList;
        this.dllList = dllList;
        this.mainClass = mainClass;
    }

    static AppProperties updateAppFiles(ILoaderGui gui, IFileLoader fileLoader, String application) throws IOException {
        File list = fileLoader.receiveFile(application + "_jars.list", false).file;
        if (list == null)
            return null;
        fileLoader.receiveFile(AppCommon.getSplashName(application), true, true);
        return new AppPropertiesLoader(gui, fileLoader).load(list);
    }
}
