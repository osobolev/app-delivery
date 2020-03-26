package sample.dao;

import sqlg3.annotations.Business;
import sqlg3.annotations.SQLG;
import sqlg3.runtime.GBase;
import sqlg3.runtime.GContext;

import java.sql.SQLException;
import java.sql.Timestamp;

@SQLG
public final class ClientDB extends GBase {

    public ClientDB(GContext ctx) {
        super(ctx);
    }
    
    @Business
    public Timestamp getTime() throws SQLException {
        return new Timestamp(System.currentTimeMillis());
    }
}
