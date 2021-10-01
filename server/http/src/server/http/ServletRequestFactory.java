package server.http;

import sqlg3.remote.server.IHttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ServletRequestFactory {

    IHttpRequest newRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
