package sample;

import sqlg3.remote.common.SQLGLogger;
import sqlg3.remote.server.SessionFactory;
import sqlg3.runtime.ConnectionManager;
import sqlg3.runtime.SingleConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class SimpleSessionFactory implements SessionFactory {

    protected final String jdbcDriver;
    protected final String jdbcUrl;

    public SimpleSessionFactory(String jdbcDriver, String jdbcUrl) {
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
    }

    public SessionData login(SQLGLogger logger, String login, String password) throws SQLException {
        Connection connection = SingleConnectionManager.openConnection(jdbcDriver, jdbcUrl, login, password);
        ConnectionManager cman = new SingleConnectionManager(connection);
        return new SessionData(cman, login);
    }
}
