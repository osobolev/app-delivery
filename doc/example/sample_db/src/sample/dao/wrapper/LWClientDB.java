package sample.dao.wrapper;

import sample.dao.*;

import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import static sample.dao.ClientDB.*;

import sqlg2.LocalWrapperBase;
import sqlg2.db.InternalTransaction;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"MissortedModifiers", "UnnecessaryFullyQualifiedName", "MethodParameterNamingConvention", "InstanceVariableNamingConvention", "LocalVariableNamingConvention", "RedundantSuppression"})
public final class LWClientDB extends LocalWrapperBase implements IClientDB {

    private final ClientDB _db;

    public LWClientDB(InternalTransaction trans, boolean inline) {
        super(trans, inline);
        this._db = new ClientDB(this);
    }

    synchronized public Timestamp getTime() throws SQLException {
        boolean _ok = false;
        long _t0 = System.currentTimeMillis();
        try {
            Timestamp _ret = _db.getTime();
            _ok = true;
            return _ret;
        } finally {
            _db.closeStatements();
            _db.traceSql(_ok, _t0);
            endTransaction(_ok);
        }
    }
}
