package apploader;

import apploader.common.AppCommon;
import apploader.common.AppStreamUtils;
import apploader.common.ConfigReader;
import apploader.common.ProxyConfig;
import apploader.lib.FileLoader;
import apploader.lib.ILoaderGui;
import apploader.lib.Result;

import java.io.Console;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
final class HeadlessGui implements ILoaderGui {

    private static final String WARNING = "Предупреждение";
    private static final String ERROR = "Ошибка";

    @Override
    public void logError(Throwable error) {
        error.printStackTrace(System.err);
    }

    @Override
    public void showStatus(String status) {
        if (!status.isEmpty()) {
            System.out.println(status);
        }
    }

    public void showError(String message) {
        System.err.println(message);
    }

    public void showWarning(String message) {
        System.err.println(message);
    }

    public void showSuccess(String message) {
        System.out.println(message);
    }

    private static String readLine() {
        Console console = System.console();
        if (console != null) {
            return console.readLine();
        } else {
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                return scanner.nextLine();
            } else {
                return null;
            }
        }
    }

    private static Integer readOption(int max) {
        while (true) {
            System.out.print("Choose an option: ");
            String line = readLine();
            if (line == null)
                return null;
            try {
                int n = Integer.parseInt(line);
                if (n >= 1 && n <= max)
                    return n - 1;
            } catch (NumberFormatException ex) {
                // ignore
            }
            System.out.println("Please input number 1-" + max);
        }
    }

    private static Result showDialogInternal(String title, String message, String[] options, Result[] values) {
        System.out.println(title + ": " + message);
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ") " + options[i]);
        }
        Integer option = readOption(options.length);
        if (option == null)
            return Result.ABORT;
        return values[option.intValue()];
    }

    public Result showError2(String message, FileLoader loader) {
        return showDialogInternal(
            ERROR, message,
            new String[] {"Отмена", "Повторить"}, new Result[] {Result.ABORT, Result.RETRY}
        );
    }

    public Result showWarning2(String message, FileLoader loader) {
        return showDialogInternal(
            WARNING, message,
            new String[] {"Продолжить", "Отмена"}, new Result[] {Result.IGNORE, Result.ABORT}
        );
    }

    public Result showWarning3(String message, FileLoader loader) {
        return showDialogInternal(
            WARNING, message,
            new String[] {"Пропустить", "Отмена", "Повторить"}, new Result[] {Result.IGNORE, Result.ABORT, Result.RETRY}
        );
    }

    public void showProxyDialog(ProxyConfig proxy, URL url, FileLoader loader) {
        System.err.println("Proxy configuration not supported in headless mode");
    }

    static URL checkURL(ProxyConfig proxy, String urlStr) throws Exception {
        URL serverUrl = ConfigReader.toServerUrl(urlStr.trim(), AppCommon.GLOBAL_APP_LIST);
        URLConnection conn = serverUrl.openConnection(proxy.proxy);
        try (InputStream is = conn.getInputStream()) {
            OutputStream consume = new OutputStream() {

                @Override
                public void write(int b) {
                }

                @Override
                public void write(byte[] b, int off, int len) {
                }
            };
            AppStreamUtils.copyStream(is, consume, -1);
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
        return serverUrl;
    }

    public URL askUrl(ProxyConfig proxy) {
        while (true) {
            System.out.print("Enter server URL: ");
            String line = readLine();
            if (line == null)
                return null;
            String urlStr = line.trim();
            if (urlStr.isEmpty())
                return null;
            try {
                return checkURL(proxy, urlStr);
            } catch (Exception ex) {
                System.out.println("Error: " + ex);
            }
        }
    }
}
