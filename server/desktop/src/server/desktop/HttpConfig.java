package server.desktop;

import server.jetty.ServerInitException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

final class HttpConfig {

    private static final String OP_PORT = "-p";
    private static final String OP_ROOT = "-d";
    private static final String OP_HELP = "-?";

    final File rootDir;
    final Integer port;

    private HttpConfig(File rootDir, Integer port) {
        this.rootDir = rootDir;
        this.port = port;
    }

    static HttpConfig parse(String[] args) throws ServerInitException {
        Map<String, Object> map = parseArgs(
            args,
            new String[] {OP_PORT, OP_ROOT, OP_HELP},
            new Class<?>[] {Integer.class, String.class, Boolean.class}
        );
        if (map.get(OP_HELP) != null) {
            System.out.println("«апуск HTTP-сервера:");
            System.out.println("http-server.bat <опции>");
            System.out.println("  " + OP_PORT + ":<порт>    - запустить сервер на этом порту");
            System.out.println("  " + OP_ROOT + ":<каталог> - корневой каталог сервера");
            System.out.println("  " + OP_HELP + "           - эта справка");
        }
        Integer port = (Integer) map.get(OP_PORT);
        String sRoot = (String) map.get(OP_ROOT);
        File rootDir = new File(".");
        if (sRoot != null) {
            rootDir = new File(sRoot);
        }
        return new HttpConfig(rootDir, port);
    }

    public static Map<String, Object> parseArgs(String[] args, String[] options, Class<?>[] types) throws ServerInitException {
        Map<String, Object> props = new HashMap<String, Object>();
        for (String arg : args) {
            String sw;
            int p = arg.indexOf(':');
            if (p < 0) {
                sw = arg.trim();
                arg = null;
            } else {
                sw = arg.substring(0, p).trim();
                arg = arg.substring(p + 1).trim();
            }
            for (int j = 0; j < options.length; j++) {
                String option = options[j];
                if (option.equalsIgnoreCase(sw)) {
                    Object value;
                    if (Integer.class.equals(types[j])) {
                        if (arg == null)
                            continue;
                        try {
                            value = Integer.valueOf(arg);
                        } catch (NumberFormatException ex) {
                            throw new ServerInitException("Ќе число дл€ " + option + ": " + arg);
                        }
                    } else if (Boolean.class.equals(types[j])) {
                        value = Boolean.TRUE;
                    } else {
                        if (arg == null)
                            continue;
                        value = arg;
                    }
                    props.put(option, value);
                    break;
                }
            }
        }
        return props;
    }
}
