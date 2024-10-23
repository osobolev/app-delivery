package server.war;

import server.http.ServletRequestFactory;
import txrpc.remote.common.body.ISerializer;
import txrpc.remote.common.body.JavaSerializer;
import txrpc.remote.server.IHttpRequest;
import txrpc.remote.server.body.BodyHttpRequest;
import txrpc.remote.server.body.ServerBodyInteraction;

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
        return new BodyHttpRequest(req.getRemoteHost(), new ServerBodyInteraction(
            serializer, req.getInputStream(), resp.getOutputStream()
        ));
    }
}
