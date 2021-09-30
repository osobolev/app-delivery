package server.http;

import sqlg3.remote.server.HttpDispatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class SessionUtil {

    public static void writeSessionInfo(OutputStream output, HttpDispatcher http) throws IOException {
        output.write((http == null ? "" : http.getApplication()).getBytes(StandardCharsets.UTF_8));
    }
}
