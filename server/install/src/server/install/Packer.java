package server.install;

import apploader.common.AppCommon;
import server.install.packers.InnoPacker;
import server.install.packers.MakeselfPacker;
import server.install.packers.RarPacker;
import server.install.packers.ZipPacker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Supplier;

public interface Packer {

    String getResultFileName(String baseName);

    /**
     * @param countFiles количество файлов в папке buildDir
     */
    boolean buildResultFile(BuildInfo info, PercentCell percentCell, int countFiles, File result) throws IOException;

    static void addPackers(List<Packer> packers, boolean windowsClient) {
        if (AppCommon.isWindows() == windowsClient) {
            if (windowsClient) {
                packers.add(new InnoPacker());
                packers.add(new RarPacker());
            } else {
                packers.add(new RarPacker());
                packers.add(new MakeselfPacker());
            }
        }
        packers.add(new ZipPacker());
    }

    static void parsePackers(List<Packer> packers, String packerStr) {
        Map<String, Supplier<Packer>> byName = new HashMap<>();
        byName.put("inno", InnoPacker::new);
        byName.put("rar", RarPacker::new);
        byName.put("makeself", MakeselfPacker::new);
        byName.put("zip", ZipPacker::new);

        StringTokenizer tok = new StringTokenizer(packerStr, ",");
        boolean wasZip = false;
        while (tok.hasMoreTokens()) {
            String name = tok.nextToken().trim().toLowerCase();
            Supplier<Packer> packerSupplier = byName.get(name);
            if (packerSupplier != null) {
                Packer packer = packerSupplier.get();
                if (packer instanceof ZipPacker) {
                    wasZip = true;
                }
                packers.add(packer);
            }
        }
        if (!wasZip) {
            packers.add(new ZipPacker());
        }
    }
}
