package server.install.packers;

import server.install.*;

import java.io.File;
import java.io.IOException;

public final class InnoPacker implements Packer {

    @Override
    public String getResultFileName() {
        return "install.exe";
    }

    @Override
    public boolean buildResultFile(BuildInfo info, PercentCell percentCell, File result) throws IOException {
        File iscc = info.findExe("inno.dir", "Inno Setup 5", "ISCC.exe");
        if (iscc == null)
            return false;

        String issScript = "client.iss";
        File iss = info.getSource(issScript);
        File issCommon = info.getSource("common.iss");
        if (iss.isFile() && issCommon.isFile()) {
            info.copyToTarget(iss);
            info.copyToTarget(issCommon);
        } else {
            return false;
        }

        String[] args = {
            iscc.getAbsolutePath(),
            issScript
        };
        Process process = Runtime.getRuntime().exec(args, null, info.buildDir);
        new Thread(new CountOutputEater(process.getInputStream(), info.countFiles * 2 + 70, percentCell)).start();
        new Thread(new OutputEater(process.getErrorStream())).start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0)
                return false;
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        return true;
    }
}
