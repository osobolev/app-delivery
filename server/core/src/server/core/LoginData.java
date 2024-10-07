package server.core;

import txrpc.runtime.SingleConnectionManager;

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

    public LoginData(String url, String user, String password) {
        this(null, url, user, password);
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
}
