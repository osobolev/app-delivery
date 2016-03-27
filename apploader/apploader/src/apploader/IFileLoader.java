package apploader;

import apploader.client.Application;

import java.util.List;

abstract class IFileLoader {

    final FileResult receiveFile(String file, boolean silent) {
        return receiveFile(file, silent, false);
    }

    abstract FileResult receiveFile(String file, boolean silent, boolean noTrace);

    abstract List<Application> loadApplications(String file, String app);
}
