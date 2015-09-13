package org.sluckframework.cqrs.saga.repository.jdbc;

import org.sluckframework.common.serializer.SerializedObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * jdbc saga sql schema
 *
 * Author: sunxy
 * Created: 2015-09-13 21:55
 * Since: 1.0
 */
public interface SagaSqlSchema {

    /**
     * 加载saga
     *
     * @param connection The connection to create the PreparedStatement for
     * @param sagaId     The identifier of the Saga to return
     * @return a statement, that creates a result set to be processed by {@link #readSerializedSaga(java.sql.ResultSet)},
     * when executed
     */
    PreparedStatement sql_loadSaga(Connection connection, String sagaId) throws SQLException;

    /**
     * remove 关联值
     *
     * @param connection     The connection to create the PreparedStatement for
     * @param key            The key of the association to remove
     * @param value          The value of the association to remove
     * @param sagaType       The type of saga to remove the association for
     * @param sagaIdentifier The identifier of the Saga to remove the association for
     * @return a statement that removes the association value, when executed
     */
    PreparedStatement sql_removeAssocValue(Connection connection, String key, String value, String sagaType,
                                           String sagaIdentifier) throws SQLException;

    /**
     *存储 关联值
     *
     * @param connection     The connection to create the PreparedStatement for
     * @param key            The key of the association to store
     * @param value          The value of the association to store
     * @param sagaType       The type of saga to create the association for
     * @param sagaIdentifier The identifier of the Saga to create the association for
     * @return a statement that inserts the association value, when executed
     */
    PreparedStatement sql_storeAssocValue(Connection connection, String key, String value, String sagaType,
                                          String sagaIdentifier) throws SQLException;

    /**
     * 通过关联值找到saga标示符
     *
     * @param connection The connection to create the PreparedStatement for
     * @param key        The key of the association
     * @param value      The value of the association
     * @param sagaType   The type of saga to find associations for
     * @return a PreparedStatement that creates a ResultSet containing only saga identifiers when executed
     */
    PreparedStatement sql_findAssocSagaIdentifiers(Connection connection, String key, String value, String sagaType)
            throws SQLException;

    /**
     * 删除 saga
     *
     * @param connection     The connection to create the PreparedStatement for
     * @param sagaIdentifier The identifier of the Saga to remove
     * @return a statement that deletes the Saga, when executed
     */
    PreparedStatement sql_deleteSagaEntry(Connection connection, String sagaIdentifier) throws SQLException;

    /**
     * 删除 所有与指定 saga 相关的关联值
     *
     * @param connection     The connection to create the PreparedStatement for
     * @param sagaIdentifier The identifier of the Saga to remove associations for
     * @return a statement that deletes the associations, when executed
     */
    PreparedStatement sql_deleteAssociationEntries(Connection connection, String sagaIdentifier) throws SQLException;

    /**
     * 更新 saga
     *
     * @param connection     The connection to create the PreparedStatement for
     * @param sagaIdentifier The identifier of the Saga to update
     * @param serializedSaga The serialized form of the saga to update
     * @param sagaType       The serialized type of the saga
     * @param revision       The revision identifier of the serialized form
     * @return a statement that updates a Saga entry, when executed
     */
    PreparedStatement sql_updateSaga(Connection connection, String sagaIdentifier, byte[] serializedSaga,
                                     String sagaType, String revision) throws SQLException;

    /**
     * insert new saga
     * @param connection     The connection to create the PreparedStatement for
     * @param sagaIdentifier The identifier of the Saga to insert
     * @param serializedSaga The serialized form of the saga to insert
     * @param sagaType       The serialized type of the saga
     * @param revision       The revision identifier of the serialized form
     * @return a statement that inserts a Saga entry, when executed
     */
    PreparedStatement sql_storeSaga(Connection connection, String sagaIdentifier, String revision, String sagaType,
                                    byte[] serializedSaga) throws SQLException;

    /**
     * 创建表来存储 saga 关联值
     *
     * @param connection The connection to create the PreparedStatement for
     * @return a Prepared statement that created the Association Value table, when executed
     */
    PreparedStatement sql_createTableAssocValueEntry(Connection connection) throws SQLException;

    /**
     * 创建 存储 saga的表
     * @param connection The connection to create the PreparedStatement for
     * @return a Prepared statement that created the Saga table, when executed
     */
    PreparedStatement sql_createTableSagaEntry(Connection connection) throws SQLException;

    /**
     * 从 resultSet 中读取被 序列化的 saga
     *
     * @param resultSet The result set to read data from.
     * @return a SerializedObject, containing the serialized data from the resultSet
     */
    SerializedObject<?> readSerializedSaga(ResultSet resultSet) throws SQLException;
}
