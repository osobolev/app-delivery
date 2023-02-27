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

        String startupScript = info.getProperty("makeself.script", "makeself-setup.sh");
        String label = info.getProperty("base.name", "client");
        File scriptFile = info.getSource(startupScript);
        String[] args;
        if (!scriptFile.isFile()) {
            String dir = info.getProperty("makeself.dir", null);
            if (dir == null) {
                info.log("Neither makeself script " + scriptFile.getAbsolutePath() + " found nor makeself.dir property specified");
                return false;
            }
            args = new String[] {
                makeself.getAbsolutePath(),
                "--notemp",
                "--target", dir,
                info.buildDir.getAbsolutePath(),
                result.getAbsolutePath(),
                label
            };
        } else {
            info.copyToTarget(scriptFile);
            args = new String[] {
                makeself.getAbsolutePath(),
                info.buildDir.getAbsolutePath(),
                result.getAbsolutePath(),
                label,
                "./" + startupScript
            };
        }
        for (String app : info.apps) {
            info.copyToTargetIfExists(app + ".png");
        }

        Process process = Runtime.getRuntime().exec(args, null, info.root);
        Charset charset = StandardCharsets.UTF_8;
        new Thread(new CountOutputEater(process.getInputStream(), countFiles + 2, percentCell, charset)).start();
        new Thread(new OutputEater(process.getErrorStream())).start();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        return true;
    }
}
