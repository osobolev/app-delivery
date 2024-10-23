package server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class SessionUtil {

    public static void writeSessionInfo(OutputStream output) throws IOException {
        output.write("app-delivery".getBytes(StandardCharsets.UTF_8));
    }
}
