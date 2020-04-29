package server.core;

import sqlg3.runtime.SingleConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public final class LoginData {

    private final String driver;
    private final String url;
    private final String user;
    private final String password;

    public LoginData(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
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

    public void testConnection() throws SQLException {
        Connection connection = SingleConnectionManager.openConnection(driver, url, user, password);
        connection.close();
    }

    public static String getDriver(String driver) {
        return driver == null ? "oracle.jdbc.driver.OracleDriver" : driver;
    }
}
