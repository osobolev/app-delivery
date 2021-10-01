package server.war;

import server.http.ServletRequestFactory;
import server.http.SessionUtil;
import sqlg3.remote.server.HttpDispatcher;
import sqlg3.remote.server.IHttpRequest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class SingleAppServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        ServletContext ctx = getServletContext();
        ServletRequestFactory requestFactory = InitListener.getRequestFactory(ctx);
        IHttpRequest request = requestFactory.newRequest(req, resp);
        HttpDispatcher http = InitListener.getHttpDispatcher(ctx);
        if (http == null) {
            String error = "Ошибка при инициализации сервера";
            InitListener.getLogger(ctx).error(error);
            request.writeError(new SQLException(error));
        } else {
            http.dispatch(req.getRemoteHost(), request);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        HttpDispatcher http = InitListener.getHttpDispatcher(getServletContext());
        SessionUtil.writeSessionInfo(resp.getOutputStream(), http);
    }
}
