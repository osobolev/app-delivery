package server.install;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

abstract class JavaSource {

    abstract int copyJava(PercentCell percentCell, File dest) throws BuildException, IOException;

    private static File getFile(File root, String name) {
        File path = new File(name);
        if (path.isAbsolute()) {
            return path;
        } else {
            return new File(root, name);
        }
    }

    static JavaSource create(File root, Properties profileProps) {
        String javaZip = profileProps.getProperty("java.zip");
        if (javaZip != null) {
            File zip = getFile(root, javaZip);
            if (zip.isFile())
                return new JavaUnpacker(zip);
        }
        String javaDir = profileProps.getProperty("java.dir");
        if (javaDir != null) {
            File dir = getFile(root, javaDir);
            if (dir.isDirectory())
                return new JavaCopier(dir);
        }
        File javaHome = new File(System.getProperty("java.home"));
        return new JavaCopier(javaHome);
    }
}
