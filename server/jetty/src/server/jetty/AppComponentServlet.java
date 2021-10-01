package server.jetty;

import sqlg3.remote.common.UnrecoverableRemoteException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

final class AppComponentServlet extends HttpServlet {

    private final AppServerComponent component;

    AppComponentServlet(AppServerComponent component) {
        this.component = component;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String addr = request.getRemoteAddr();
        if (!component.accept(addr)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            component.dispatch(request, response);
        } catch (UnrecoverableRemoteException ex) {
            component.error("BAD REQUEST: " + addr);
            throw ex;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        component.showSessions(response.getOutputStream());
    }
}
