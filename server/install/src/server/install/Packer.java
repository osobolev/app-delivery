package server.install;

import apploader.common.AppCommon;
import server.install.packers.*;

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
                packers.add(new RarPacker(windowsClient));
                packers.add(new P7zPacker(true, windowsClient));
            } else {
                packers.add(new RarPacker(windowsClient));
                packers.add(new P7zPacker(true, windowsClient));
                packers.add(new MakeselfPacker());
            }
        } else {
            packers.add(new P7zPacker(true, windowsClient));
        }
        if (windowsClient) {
            packers.add(new ZipPacker());
            packers.add(new P7zPacker(false, windowsClient));
        } else {
            packers.add(new P7zPacker(false, windowsClient));
            packers.add(new ZipPacker());
        }
    }

    static void parsePackers(List<Packer> packers, String packerStr, boolean windowsClient) {
        Map<String, Supplier<Packer>> byName = new HashMap<>();
        byName.put("inno", InnoPacker::new);
        byName.put("rar", () -> new RarPacker(windowsClient));
        byName.put("makeself", MakeselfPacker::new);
        byName.put("7z", () -> new P7zPacker(false, windowsClient));
        byName.put("7zsfx", () -> new P7zPacker(true, windowsClient));
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
