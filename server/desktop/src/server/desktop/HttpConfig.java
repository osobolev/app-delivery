package server.desktop;

import server.jetty.ServerInitException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

final class HttpConfig {

    private static final String OP_PORT = "-p";
    private static final String OP_CTX  = "-c";
    private static final String OP_ROOT = "-d";
    private static final String OP_HELP = "-?";

    final File rootDir;
    final Integer port;
    final String context;

    private HttpConfig(File rootDir, Integer port, String context) {
        this.rootDir = rootDir;
        this.port = port;
        this.context = context;
    }

    static HttpConfig parse(String[] args) throws ServerInitException {
        Map<String, Object> map = parseArgs(
            args,
            new String[] {OP_PORT, OP_CTX, OP_ROOT, OP_HELP},
            new Class<?>[] {Integer.class, String.class, String.class, Boolean.class}
        );
        if (map.get(OP_HELP) != null) {
            System.out.println("Запуск HTTP-сервера:");
            System.out.println("http-server.bat <опции>");
            System.out.println("  " + OP_PORT + ":<порт>     - запустить сервер на этом порту");
            System.out.println("  " + OP_CTX  + ":<контекст> - контекст сервера");
            System.out.println("  " + OP_ROOT + ":<каталог>  - корневой каталог сервера");
            System.out.println("  " + OP_HELP + "            - эта справка");
        }
        Integer port = (Integer) map.get(OP_PORT);
        String context = (String) map.get(OP_CTX);
        String root = (String) map.get(OP_ROOT);
        File rootDir;
        if (root != null) {
            rootDir = new File(root);
        } else {
            rootDir = new File(".");
        }
        return new HttpConfig(rootDir, port, context);
    }

    public static Map<String, Object> parseArgs(String[] args, String[] options, Class<?>[] types) throws ServerInitException {
        Map<String, Object> props = new HashMap<>();
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
                            throw new ServerInitException("Не число для " + option + ": " + arg);
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
