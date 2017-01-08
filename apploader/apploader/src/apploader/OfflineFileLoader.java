package apploader;

import apploader.client.Application;

import java.io.File;
import java.util.Collections;
import java.util.List;

final class OfflineFileLoader extends IFileLoader {

    private final LoaderGui gui;

    OfflineFileLoader(LoaderGui gui) {
        this.gui = gui;
    }

    FileResult receiveFile(String file, boolean silent, boolean noTrace) {
        File local = new File(file).getAbsoluteFile();
        if (local.exists()) {
            return new FileResult(local, false, false);
        } else {
            if (!silent) {
                gui.showError("Файл " + file + " отсутствует");
            }
            return new FileResult(null, false, false);
        }
    }

    List<Application> loadApplications(String file, String app) {
        return Collections.singletonList(new Application(app, app));
    }
}
