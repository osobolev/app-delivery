package apploader.lib;

import apploader.common.Application;

import java.util.List;

public abstract class IFileLoader {

    public final FileResult receiveFile(String file, boolean silent) {
        return receiveFile(file, silent, false);
    }

    public abstract FileResult receiveFile(String file, boolean silent, boolean noTrace);

    public abstract List<Application> loadApplications(String file, String app);
}
