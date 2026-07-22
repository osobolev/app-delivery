package apploader.lib;

import apploader.common.AppCommon;
import apploader.common.AppStreamUtils;
import apploader.common.AppZip;
import apploader.common.ConfigReader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

final class ClientUpdater {

    private static String escape(String path) {
        return "\"" + path + "\"";
    }

    private static void moveFiles(PrintWriter pw, boolean windows, File from,
                                  String prefixFrom, String prefixTo,
                                  Predicate<String> except) {
        File[] files = from.listFiles();
        if (files == null)
            return;
        for (File file : files) {
            String name = file.getName();
            if (except.test(name))
                continue;
            if (windows) {
                pw.println("move /y " + escape(prefixFrom + name) + " " + escape(prefixTo + name) + " >nul");
            } else {
                pw.println("mv -f " + escape(prefixFrom + name) + " " + escape(prefixTo + name));
            }
        }
    }

    private static void moveScript(File newDir) throws IOException {
        boolean windows = AppCommon.isWindows();
        String scriptName = windows ? "client.update.bat" : "client.update.sh";
        File script = new File(newDir, scriptName);
        AppCommon.generateNativeFile(script, windows, pw -> {
            String backup = "backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss"));
            if (windows) {
                pw.println("@echo off");
                pw.println("set BACKUP=" + backup);
                pw.println("rmdir /s /q %BACKUP%");
                pw.println("mkdir %BACKUP%");
            } else {
                pw.println("#!/usr/bin/env sh");
                pw.println("BACKUP=" + backup);
                pw.println("rm -rf $BACKUP");
                pw.println("mkdir $BACKUP");
            }

            String prefixTo = windows ? "%BACKUP%\\" : "$BACKUP/";
            moveFiles(pw, windows, newDir.getParentFile(), "", prefixTo, name -> {
                if ("client.new".equals(name) || "client.new.tmp".equals(name) || backup.equals(name))
                    return true;
                if ("checknew.bat".equals(name) || "checknew.sh".equals(name))
                    return true;
                if (name.startsWith("unins000."))
                    return true;
                if ("options.local.bat".equals(name) || "options.local.sh".equals(name) || "local.vmoptions".equals(name))
                    return true;
                return false;
            });

            String prefixFrom = "client.new" + (windows ? "\\" : "/");
            moveFiles(pw, windows, newDir, prefixFrom, "", scriptName::equals);

            if (windows) {
                pw.println("rmdir /s /q client.new");
            } else {
                pw.println("rm -rf client.new");
            }
        });
    }

    static void unzipClient(File tmpDir, File zip, IntConsumer progress) throws IOException {
        Path destDir = tmpDir.toPath();
        AppStreamUtils.deleteAll(destDir);
        Files.createDirectories(destDir);
        int[] fileCount = new int[1];
        int[] counter = new int[1];
        AppZip appZip = AppZip.create(zip);
        appZip.unpackWithExtra(
            destDir,
            zipFile -> fileCount[0] = zipFile.size(),
            entry -> {
                int percent = Math.round((float) counter[0]++ / fileCount[0] * 100f);
                progress.accept(percent);
            }
        );
    }

    static void prepareUpdate(File tmpDir, File newDir, String newMajorVersion) throws IOException {
        moveScript(tmpDir);
        Files.write(tmpDir.toPath().resolve(AppCommon.MAJOR_VERSION), newMajorVersion.getBytes(StandardCharsets.UTF_8));
        Files.move(tmpDir.toPath(), newDir.toPath());
    }

    static String createClientZip(HttpInteraction http, URL zipURL, IntConsumer progress) throws IOException {
        while (true) {
            String response = http.interact(zipURL, conn -> {
                conn.setDoOutput(true);
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).setRequestMethod("POST");
                }
                conn.setRequestProperty("Accept", "text/plain");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                AppStreamUtils.copyStream(conn.getInputStream(), bos, -1, null);
                return bos.toString("UTF-8").trim();
            });
            if ("OK".equals(response)) {
                progress.accept(100);
                return null;
            } else if (response.startsWith("PRC ")) {
                try {
                    int percent = Integer.parseInt(response.substring(4));
                    progress.accept(percent);
                } catch (NumberFormatException ex) {
                    // ignore
                }
            } else {
                String error;
                if (response.startsWith("ERR ")) {
                    error = response.substring(4);
                } else {
                    error = "Неизвестная ошибка";
                }
                return error;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                return "Прервано";
            }
        }
    }

    static List<ClientProfile> listProfiles(URL base, HttpInteraction http) throws IOException {
        String params = String.format(
            "%s=%s&%s=%s",
            AppCommon.PROFILE_WINDOWS, AppCommon.isWindows(),
            AppCommon.PROFILE_BITS, AppCommon.getOSBits()
        );
        URL url = AppCommon.resolve(base, AppCommon.PROFILE_LIST + "?" + params);
        return http.interact(url, conn -> {
            Map<String, ClientProfile> profiles = new LinkedHashMap<>();
            try (InputStream is = conn.getInputStream()) {
                ConfigReader.readConfig(is, (left, right) -> {
                    profiles.put(left, new ClientProfile(left, right));
                    return true;
                });
            }
            if (profiles.isEmpty()) {
                return Collections.singletonList(ClientProfile.DEFAULT);
            } else {
                return new ArrayList<>(profiles.values());
            }
        });
    }

    static String needsUpdate(URL base, HttpInteraction http, File majorVersionFile) {
        String majorVersion = null;
        try {
            URL url = AppCommon.resolve(base, AppCommon.MAJOR_VERSION);
            majorVersion = http.interact(url, conn -> {
                conn.connect();
                if (conn instanceof HttpURLConnection) {
                    int code = ((HttpURLConnection) conn).getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK)
                        return null;
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (InputStream is = conn.getInputStream()) {
                    AppStreamUtils.copyStream(is, bos, -1, null);
                }
                return bos.toString("UTF-8").trim();
            });
        } catch (IOException ex) {
            // ignore
        }
        if (majorVersion == null)
            return null;
        String localMajorVersion = null;
        if (majorVersionFile.exists()) {
            try {
                localMajorVersion = new String(Files.readAllBytes(majorVersionFile.toPath()), StandardCharsets.UTF_8).trim();
            } catch (IOException ex) {
                // ignore
            }
        }
        if (majorVersion.equals(localMajorVersion))
            return null;
        return majorVersion;
    }
}
