package server.core;

import sqlg3.runtime.DBSpecific;
import sqlg3.runtime.RuntimeMapper;
import sqlg3.runtime.RuntimeMapperImpl;
import sqlg3.runtime.SingleConnectionManager;
import sqlg3.runtime.specific.Generic;

import java.sql.Connection;
import java.sql.SQLException;

public final class LoginData {

    private final String driver;
    private final String url;
    private final String user;
    private final String password;
    private final DBSpecific specific;
    private final RuntimeMapper mappers;

    public LoginData(String driver, String url, String user, String password, DBSpecific specific, RuntimeMapper mappers) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.specific = specific;
        this.mappers = mappers;
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

    public RuntimeMapper getMappers() {
        return mappers;
    }

    public void testConnection() throws SQLException {
        Connection connection = SingleConnectionManager.openConnection(driver, url, user, password);
        connection.close();
    }

    public static String getDriver(String driver) {
        return driver == null ? "oracle.jdbc.driver.OracleDriver" : driver;
    }

    public static DBSpecific getSpecific(String specClass) throws Exception {
        return specClass == null ? new Generic() : (DBSpecific) Class.forName(specClass).newInstance();
    }

    public static RuntimeMapper getMappers(String mapperClass) throws Exception {
        return mapperClass == null ? new RuntimeMapperImpl() : (RuntimeMapper) Class.forName(mapperClass).newInstance();
    }
}
