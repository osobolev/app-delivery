package sample;

import sqlg2.db.ConnectionManager;
import sqlg2.db.SQLGLogger;
import sqlg2.db.SessionFactory;
import sqlg2.db.SingleConnectionManager;

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
