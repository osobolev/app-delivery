package server.http;

import sqlg3.remote.common.ISerializer;
import sqlg3.remote.common.JavaSerializer;
import sqlg3.remote.server.BodyHttpRequest;
import sqlg3.remote.server.IHttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class DefaultRequestFactory implements ServletRequestFactory {

    private final ISerializer serializer;

    public DefaultRequestFactory(ISerializer serializer) {
        this.serializer = serializer;
    }

    public DefaultRequestFactory() {
        this(new JavaSerializer());
    }

    @Override
    public IHttpRequest newRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return new BodyHttpRequest(serializer, req.getInputStream(), resp.getOutputStream());
    }
}
