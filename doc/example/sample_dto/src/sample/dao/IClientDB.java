package sample.dao;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings("UnnecessaryFullyQualifiedName")
@txrpc.api.Impl("sample.dao.ClientDB")
public interface IClientDB extends txrpc.api.IDBCommon {

    java.sql.Timestamp getTime() throws java.sql.SQLException;
}
