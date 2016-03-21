package server.http;

import sqlg2.db.HttpDispatcher;
import sqlg2.db.SessionInfo;

import java.io.IOException;
import java.io.OutputStream;

public final class SessionUtil {

    private static final String ENCODING = "UTF-8";

    private static String lpad2(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    public static void writeSessionInfo(OutputStream output, HttpDispatcher dispatcher, String application) throws IOException {
        SessionInfo[] sessions = dispatcher == null ? new SessionInfo[0] : dispatcher.getActiveSessions();
        StringBuilder buf = new StringBuilder();
        buf.append("<html>\n");
        buf.append("<head>\n");
        buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + ENCODING + "\">");
        String title = "Активные сессии для приложения " + application;
        buf.append("<title>" + title + "</title>");
        buf.append("</head>\n");
        buf.append("<body>\n");
        buf.append("<h2>" + title + "</h2>\n");
        buf.append("<table border=1>\n");
        buf.append("<tr>\n");
        buf.append("<th>ID сессии</th>\n");
        buf.append("<th>Пользователь</th>\n");
        buf.append("<th>Адрес</th>\n");
        buf.append("<th>Время работы</th>\n");
        buf.append("<th>Фоновый процесс</th>\n");
        buf.append("</tr>\n");
        for (SessionInfo session : sessions) {
            buf.append("<tr>\n");
            buf.append("<td>").append(session.sessionOrderId).append("</td>\n");
            buf.append("<td>").append(session.user).append("</td>\n");
            buf.append("<td>").append(session.host).append("</td>\n");
            long total = session.workingTime / 1000;
            int seconds = (int) (total % 60);
            total /= 60;
            int minutes = (int) (total % 60);
            long hours = total / 60;
            String time = hours + ":" + lpad2(minutes) + ":" + lpad2(seconds);
            buf.append("<td>").append(time).append("</td>\n");
            buf.append("<td>").append(session.background ? "Да" : "&nbsp;").append("</td>\n");
            buf.append("</tr>\n");
        }
        buf.append("</table>");
        buf.append("</body>\n");
        buf.append("</html>\n");
        output.write(buf.toString().getBytes(ENCODING));
    }
}
