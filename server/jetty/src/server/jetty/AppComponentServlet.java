package server.jetty;

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
        response.setStatus(HttpServletResponse.SC_OK);
        component.dispatch(request.getRemoteHost(), request.getInputStream(), response.getOutputStream());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        component.showSessions(response.getOutputStream());
    }
}
