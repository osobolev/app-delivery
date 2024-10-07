package server.http;

import txrpc.remote.common.ISerializer;
import txrpc.remote.common.JavaSerializer;
import txrpc.remote.server.BodyHttpRequest;
import txrpc.remote.server.IHttpRequest;

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
        return new BodyHttpRequest(serializer, req.getRemoteHost(), req.getInputStream(), resp.getOutputStream());
    }
}
