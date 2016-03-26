package server.core;

import sqlg2.db.DBSpecific;
import sqlg2.db.SingleConnectionManager;
import sqlg2.db.specific.Oracle;

import java.sql.Connection;
import java.sql.SQLException;

public final class LoginData {

    private final String driver;
    private final String url;
    private final String user;
    private final String password;
    private final DBSpecific specific;

    public LoginData(String driver, String url, String user, String password, DBSpecific specific) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.specific = specific;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public DBSpecific getSpecific() {
        return specific;
    }

    public void testConnection() throws SQLException {
        Connection connection = SingleConnectionManager.openConnection(driver, url, user, password);
        connection.close();
    }

    public static String getDriver(String driver) {
        return driver == null ? "oracle.jdbc.driver.OracleDriver" : driver;
    }

    public static DBSpecific getSpecific(String specClass) throws Exception {
        return specClass == null ? new Oracle() : (DBSpecific) Class.forName(specClass).newInstance();
    }
}
