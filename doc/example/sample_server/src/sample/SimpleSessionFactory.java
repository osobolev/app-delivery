package sample;

import txrpc.runtime.*;

import java.sql.Connection;
import java.sql.SQLException;

public class SimpleSessionFactory implements SessionFactory {

    protected final String jdbcDriver;
    protected final String jdbcUrl;

    public SimpleSessionFactory(String jdbcDriver, String jdbcUrl) {
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public SessionContext login(TxRpcLogger logger, long sessionId, String login, String password) throws SQLException {
        Connection connection = SingleConnectionManager.openConnection(jdbcDriver, jdbcUrl, login, password);
        ConnectionManager cman = new SingleConnectionManager(connection);
        return new SessionContext(cman, login, null);
    }
}
