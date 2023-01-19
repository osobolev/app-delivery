package server.install.packers;

import server.install.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class MakeselfPacker implements Packer {

    @Override
    public String getResultFileName(String baseName) {
        return baseName + ".sh";
    }

    @Override
    public boolean buildResultFile(BuildInfo info, PercentCell percentCell, int countFiles, File result) throws IOException {
        File makeself = info.findLinuxExe(null, "makeself", "makeself", "makeself.sh");
        if (makeself == null) {
            info.log("Makeself executable not found");
            return false;
        }

        String startupScript = info.getProperty("makeself.script", "init-install.sh");
        File scriptFile = info.getSource(startupScript);
        if (!scriptFile.isFile()) {
            info.log("Makeself startup script not found: '" + scriptFile.getAbsolutePath() + "'");
            return false;
        } else {
            info.copyToTarget(scriptFile);
        }

        String[] args = {
            makeself.getAbsolutePath(),
            info.buildDir.getAbsolutePath(),
            result.getAbsolutePath(),
            "atis",
            "./" + startupScript
        };
        Process process = Runtime.getRuntime().exec(args, null, info.root);
        Charset charset = StandardCharsets.UTF_8;
        new Thread(new CountOutputEater(process.getErrorStream(), countFiles + 2, percentCell, charset)).start();
        new Thread(new OutputEater(process.getInputStream())).start();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        return true;
    }
}
