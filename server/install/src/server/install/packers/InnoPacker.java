package server.install.packers;

import server.install.*;

import java.io.File;
import java.io.IOException;

public final class InnoPacker implements Packer {

    @Override
    public String getResultFileName(String baseName) {
        return baseName + ".exe";
    }

    @Override
    public boolean buildResultFile(BuildInfo info, PercentCell percentCell, File result) throws IOException {
        File iscc = info.findWindowsExe("inno.dir", "Inno Setup 5", "ISCC.exe");
        if (iscc == null) {
            info.log("ISS executable not found");
            return false;
        }

        String issScript = "client.iss";
        File iss = info.getSource(issScript);
        File issCommon = info.getSource("common.iss");
        if (!iss.isFile()) {
            info.log("ISS file not found: '" + iss.getAbsolutePath() + "'");
            return false;
        } else if (!issCommon.isFile()) {
            info.log("ISS file not found: '" + issCommon.getAbsolutePath() + "'");
            return false;
        } else {
            info.copyToTarget(iss);
            info.copyToTarget(issCommon);
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
            if (exitCode != 0) {
                info.log("ISS exit code: " + exitCode);
                return false;
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        File installer = new File(result.getParentFile(), "install.exe");
        if (!installer.equals(result)) {
            return installer.renameTo(result);
        } else {
            return true;
        }
    }
}
