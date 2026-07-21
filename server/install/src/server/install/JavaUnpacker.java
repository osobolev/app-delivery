package server.install;

import apploader.common.AppZip;

import java.io.File;
import java.io.IOException;

final class JavaUnpacker extends JavaSource {

    private final File javaZip;

    JavaUnpacker(File javaZip) {
        this.javaZip = javaZip;
    }

    @Override
    int copyJava(PercentCell percentCell, File dest) throws IOException {
        dest.mkdirs();
        int[] fileCount = new int[1];
        int[] counter = new int[1];
        AppZip.create(javaZip).unpackWithExtra(
            dest.toPath(),
            zipFile -> fileCount[0] = zipFile.size(),
            entry -> percentCell.workPercent(0, 2, counter[0]++, fileCount[0])
        );
        return fileCount[0];
    }
}
