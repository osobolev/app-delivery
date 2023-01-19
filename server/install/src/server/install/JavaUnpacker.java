package server.install;

import apploader.common.AppZip;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class JavaUnpacker extends JavaSource {

    private final File javaZip;

    JavaUnpacker(File javaZip) {
        this.javaZip = javaZip;
    }

    private static int countFiles(File zip) throws IOException {
        try (ZipFile zipFile = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int count = 0;
            while (entries.hasMoreElements()) {
                entries.nextElement();
                count++;
            }
            return count;
        }
    }

    @Override
    int copyJava(PercentCell percentCell, File dest) throws IOException {
        int fileCount = countFiles(javaZip);
        dest.mkdirs();
        int[] counter = new int[1];
        AppZip.create(javaZip).unpackWithExtra(
            dest.toPath(),
            () -> percentCell.workPercent(0, 2, counter[0]++, fileCount)
        );
        return fileCount;
    }
}
