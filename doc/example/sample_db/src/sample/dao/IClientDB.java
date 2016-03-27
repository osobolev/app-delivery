package sample.dao;

import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import static sample.dao.ClientDB.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface IClientDB extends IDBCommon {

    public Timestamp getTime() throws SQLException;
}
