package server.install;

import java.io.File;
import java.io.IOException;

final class JavaCopier extends JavaSource {

    private final File javaHome;

    JavaCopier(File javaHome) {
        this.javaHome = javaHome;
    }

    private static int countFiles(File root) throws BuildException {
        File[] files = root.listFiles();
        if (files == null)
            throw new BuildException("JRE не найдена");
        int count = 0;
        for (File file : files) {
            count++;
            if (file.isDirectory()) {
                count += countFiles(file);
            }
        }
        return count;
    }

    private static final class Copier {

        final PercentCell percentCell;
        final int[] counter = new int[1];
        final int total;

        Copier(PercentCell percentCell, int total) {
            this.percentCell = percentCell;
            this.total = total;
        }

        void copy(File to, File from) throws IOException {
            to.mkdirs();
            File[] files = from.listFiles();
            if (files == null)
                return;
            for (File file : files) {
                File copy = new File(to, file.getName());
                if (file.isDirectory()) {
                    copy(copy, file);
                } else if (file.isFile()) {
                    IOUtils.copyFile(copy, file);
                }
                percentCell.workPercent(0, 2, counter[0]++, total);
            }
        }
    }

    @Override
    int copyJava(PercentCell percentCell, File dest) throws BuildException, IOException {
        int fileCount = countFiles(javaHome);
        new Copier(percentCell, fileCount).copy(dest, javaHome);
        return fileCount;
    }
}
