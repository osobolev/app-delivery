package server.http;

import server.install.InstallBuilder;

final class InstallState {

    final InstallBuilder builder;
    volatile String error = "";
    volatile boolean creating = false;

    InstallState(InstallBuilder builder) {
        this.builder = builder;
    }

    synchronized void createInstaller(boolean init, InstallServletBase is) {
        if (creating || builder.getReadyInstaller() != null)
            return;
        if (!init && error.length() > 0)
            return;
        builder.getPercentCell().reset();
        error = "";
        creating = true;
        Runnable createInstaller = () -> {
            try {
                builder.getInstaller();
            } catch (Exception ex) {
                error = ex.toString();
                is.getLogger().error(ex);
            } finally {
                creating = false;
            }
        };
        new Thread(createInstaller).start();
    }
}
