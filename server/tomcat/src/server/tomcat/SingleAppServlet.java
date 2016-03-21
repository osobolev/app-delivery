package server.tomcat;

import server.core.AppAuthFactory;
import server.core.LoginData;
import server.http.SessionUtil;
import sqlg2.db.HttpDispatcher;
import sqlg2.db.SQLGLogger;
import sqlg2.db.SessionFactory;
import sqlg2.db.SimpleLogger;
import sqlg2.db.specific.DBSpecific;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

public final class SingleAppServlet extends HttpServlet {

    private HttpDispatcher http = null;

    private HttpDispatcher getHttp(ServletConfig config) throws ServletException, SQLException {
        if (http == null) {
            ServletContext ctx = config.getServletContext();
            String application = ctx.getInitParameter(SingleUtil.APPLICATION);
            String authClass = ctx.getInitParameter("authFactory");
            String driver = LoginData.getDriver(ctx.getInitParameter("jdbcDriver"));
            AppAuthFactory factory;
            DBSpecific specific;
            try {
                factory = (AppAuthFactory) Class.forName(authClass).newInstance();
                specific = LoginData.getSpecific(ctx.getInitParameter("dbSpec"));
            } catch (Exception ex) {
                getServletContext().log(ex.toString(), ex);
                throw new ServletException(ex);
            }
            LoginData data = new LoginData(driver, ctx.getInitParameter("jdbcUrl"), ctx.getInitParameter("username"), ctx.getInitParameter("password"));
            data.testConnection();
            SessionFactory sf = factory.getAuthentificator(application, data);
            // todo: configurable logger
            SQLGLogger logger = new SQLGLogger() {

                private final PrintWriter pw = new PrintWriter(System.out, true);

                public void trace(String message) {
                    pw.println(message);
                }

                public void info(String message) {
                    pw.println(message);
                }

                public void error(String message) {
                    pw.println(message); // todo: add date???
                }

                public void error(Throwable error) {
                    SimpleLogger.printException(pw, error);
                }
            };
            http = new HttpDispatcher(application, sf, specific, logger);
        }
        return http;
    }

    public void destroy() {
        if (http != null) {
            http.shutdown();
            http = null;
        }
        super.destroy();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setStatus(HttpServletResponse.SC_OK);
        try {
            getHttp(this).dispatch(req.getRemoteHost(), req.getInputStream(), resp.getOutputStream());
        } catch (SQLException ex) {
            getServletContext().log(ex.toString(), ex);
            HttpDispatcher.writeResponse(resp.getOutputStream(), null, ex);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        SessionUtil.writeSessionInfo(resp.getOutputStream(), http, http == null ? "" : http.getApplication());
    }
}
