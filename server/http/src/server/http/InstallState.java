package server.http;

import server.install.InstallBuilder;

import java.io.IOException;

final class InstallState {

    final InstallBuilder builder;
    volatile String error = "";
    volatile boolean creating = false;

    InstallState(InstallBuilder builder) {
        this.builder = builder;
    }

    synchronized void createInstaller(boolean init, final InstallServletBase is) throws IOException {
        if (creating || builder.getReadyInstaller() != null)
            return;
        if (!init && error.length() > 0)
            return;
        builder.getPercentCell().reset();
        error = "";
        creating = true;
        Runnable createInstaller = new Runnable() {
            public void run() {
                try {
                    builder.getInstaller();
                } catch (Exception ex) {
                    error = ex.toString();
                    is.getLogger().error(ex);
                } finally {
                    creating = false;
                }
            }
        };
        new Thread(createInstaller).start();
    }
}
