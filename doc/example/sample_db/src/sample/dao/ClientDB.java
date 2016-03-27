package sample.dao;

import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@SQLG
public final class ClientDB extends GBase {

    public ClientDB(LocalWrapperBase lwb) {
        super(lwb);
    }
    
    @Business
    public Timestamp getTime() throws SQLException {
        /**
         * SELECT CURRENT_TIMESTAMP
         */
        @Prepare PreparedStatement stmt = prepareStatement("SELECT CURRENT_TIMESTAMP");
        return singleRowQueryReturningTimestamp(stmt);
    }
}
