package apploader;

import apploader.client.Application;

import java.util.List;

abstract class IFileLoader {

    abstract FileResult receiveFile(String file, boolean silent, boolean noTrace);

    abstract List<Application> loadApplications(String file, String app);
}
