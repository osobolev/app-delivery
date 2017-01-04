package server.install.packers;

import server.install.*;

import java.io.File;
import java.io.IOException;

public final class RarPacker implements Packer {

    @Override
    public String getResultFileName() {
        return "install.exe";
    }

    @Override
    public boolean buildResultFile(BuildInfo info, PercentCell percentCell, File result) throws IOException {
        File winRar = info.findExe("rar.dir", "WinRAR", "rar.exe");
        if (winRar == null)
            return false;

        String sfxConfig = "sfx.cfg";
        File sfx = info.getSource(sfxConfig);
        if (!sfx.isFile())
            return false;

        String[] args = {
            winRar.getAbsolutePath(),
            "a",    // add
            "-ep1", // exclude base directory from names
            "-z" + sfxConfig,
            "-sfx", // create SFX
            "-r",   // recurse subdirs
            "-m1",  // compression level to fast
            "-idp", // do not output percents
            result.getAbsolutePath(),
            info.buildDir.getAbsolutePath() + "\\*"
        };
        Process process = Runtime.getRuntime().exec(args, null, info.root);
        new Thread(new CountOutputEater(process.getInputStream(), info.countFiles + 5, percentCell)).start();
        new Thread(new OutputEater(process.getErrorStream())).start();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        return true;
    }
}
