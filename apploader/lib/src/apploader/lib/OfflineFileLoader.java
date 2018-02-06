package apploader.lib;

import apploader.common.Application;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class OfflineFileLoader extends IFileLoader {

    private final ILoaderGui gui;

    public OfflineFileLoader(ILoaderGui gui) {
        this.gui = gui;
    }

    public FileResult receiveFile(String file, boolean silent, boolean noTrace) {
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

    public List<Application> loadApplications(String file, String app) {
        return Collections.singletonList(new Application(app, app));
    }
}
