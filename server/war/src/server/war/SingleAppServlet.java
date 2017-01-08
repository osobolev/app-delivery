package server.war;

import server.http.SessionUtil;
import sqlg2.db.HttpDispatcher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class SingleAppServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        HttpDispatcher http = InitListener.getHttpDispatcher(getServletContext());
        if (http == null) {
            String error = "Ошибка при инициализации сервера";
            InitListener.getLogger(getServletContext()).error(error);
            HttpDispatcher.writeResponse(resp.getOutputStream(), null, new SQLException(error));
            return;
        }
        http.dispatch(req.getRemoteHost(), req.getInputStream(), resp.getOutputStream());
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        HttpDispatcher http = InitListener.getHttpDispatcher(getServletContext());
        SessionUtil.writeSessionInfo(resp.getOutputStream(), http, http == null ? "" : http.getApplication());
    }
}
