package server.install.packers;

import apploader.common.AppCommon;
import server.install.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class RarPacker implements Packer {

    @Override
    public String getResultFileName(String baseName) {
        if (AppCommon.isWindows()) {
            return baseName + ".exe";
        } else {
            return baseName + ".sfx";
        }
    }

    @Override
    public boolean buildResultFile(BuildInfo info, PercentCell percentCell, int countFiles, File result) throws IOException {
        File rar;
        Charset charset;
        if (AppCommon.isWindows()) {
            rar = info.findWindowsExe("rar.dir", "WinRAR", "rar.exe");
            charset = Charset.forName("Cp866");
        } else {
            rar = info.findLinuxExe("rar.dir", null, "rar");
            charset = StandardCharsets.UTF_8;
        }
        if (rar == null) {
            info.log("RAR executable not found");
            return false;
        }

        String sfxConfig = info.getProperty("rar.cfg", "sfx.cfg");
        File sfx = info.getSource(sfxConfig);
        if (!sfx.isFile()) {
            info.log("RAR SFX config file not found: '" + sfx.getAbsolutePath() + "'");
            return false;
        }

        String[] args = {
            rar.getAbsolutePath(),
            "a",    // add
            "-ep1", // exclude base directory from names
            "-z" + sfxConfig,
            "-sfx", // create SFX
            "-r",   // recurse subdirs
            "-m1",  // compression level to fast
            "-idp", // do not output percents
            result.getAbsolutePath(),
            info.buildDir.getAbsolutePath() + File.separator + "*"
        };
        Process process = Runtime.getRuntime().exec(args, null, info.root);
        new Thread(new CountOutputEater(process.getInputStream(), countFiles + 5, percentCell, charset)).start();
        new Thread(new OutputEater(process.getErrorStream())).start();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        return true;
    }
}
