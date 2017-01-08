package server.install;

import java.io.File;
import java.io.IOException;

public interface Packer {

    String getResultFileName(String baseName);

    boolean buildResultFile(BuildInfo info, PercentCell percentCell, File result) throws IOException;
}
