package server.install.packers;

import server.install.BuildInfo;
import server.install.IOUtils;
import server.install.Packer;
import server.install.PercentCell;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipPacker implements Packer {

    private static final class Zipper {

        final PercentCell percentCell;
        final int countFiles;
        final ZipOutputStream zip;
        final int[] count = new int[1];

        Zipper(PercentCell percentCell, int countFiles, ZipOutputStream zip) {
            this.percentCell = percentCell;
            this.countFiles = countFiles;
            this.zip = zip;
        }

        void addToZip(File dir, String path) throws IOException {
            File[] files = dir.listFiles();
            if (files == null)
                return;
            for (File file : files) {
                String nextPath;
                if (path == null) {
                    nextPath = file.getName();
                } else {
                    nextPath = path + "/" + file.getName();
                }
                if (file.isDirectory()) {
                    addToZip(file, nextPath);
                } else if (file.isFile()) {
                    zip.putNextEntry(new ZipEntry(nextPath));
                    IOUtils.copyFile(zip, file);
                    zip.closeEntry();
                }
                percentCell.workPercent(1, 2, count[0]++, countFiles);
            }
        }
    }

    @Override
    public String getResultFileName(String baseName) {
        return baseName + ".zip";
    }

    @Override
    public boolean buildResultFile(BuildInfo info, PercentCell percentCell, int countFiles, File result) throws IOException {
        try (OutputStream os = Files.newOutputStream(result.toPath());
             ZipOutputStream zos = new ZipOutputStream(os)) {
            new Zipper(percentCell, countFiles, zos).addToZip(info.buildDir, null);
        }
        return true;
    }
}
