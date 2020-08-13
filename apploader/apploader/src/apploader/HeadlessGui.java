package apploader;

import apploader.common.ProxyConfig;
import apploader.lib.FileLoader;
import apploader.lib.ILoaderGui;
import apploader.lib.Result;

import java.io.Console;
import java.net.URL;
import java.util.Scanner;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
final class HeadlessGui implements ILoaderGui {

    private static final String WARNING = "Предупреждение";
    private static final String ERROR = "Ошибка";

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

    private static Integer readOption(int max) {
        System.out.print("Choose an option: ");
        Console console = System.console();
        while (true) {
            String line;
            if (console != null) {
                line = console.readLine();
            } else {
                Scanner scanner = new Scanner(System.in);
                if (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                } else {
                    line = null;
                }
            }
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
}
