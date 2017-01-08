package server.install.packers;

import server.install.BuildInfo;
import server.install.IOUtils;
import server.install.Packer;
import server.install.PercentCell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipPacker implements Packer {

    private static void addToZip(PercentCell percentCell, BuildInfo info, ZipOutputStream zip, File dir, String path, int[] count) throws IOException {
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
                addToZip(percentCell, info, zip, file, nextPath, count);
            } else if (file.isFile()) {
                zip.putNextEntry(new ZipEntry(nextPath));
                IOUtils.copyFile(zip, file);
                zip.closeEntry();
            }
            percentCell.workPercent(1, 2, count[0]++, info.countFiles);
        }
    }

    @Override
    public String getResultFileName(String baseName) {
        return baseName + ".zip";
    }

    @Override
    public boolean buildResultFile(BuildInfo info, PercentCell percentCell, File result) throws IOException {
        OutputStream os = new FileOutputStream(result);
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            os = zos;
            addToZip(percentCell, info, zos, info.buildDir, null, new int[1]);
        } finally {
            IOUtils.close(os);
        }
        return true;
    }
}
