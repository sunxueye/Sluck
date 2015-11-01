package org.sluckframework.cqrs.saga.repository.jdbc;

import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.SimpleSerializedObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 通用的 sql schmea 实现
 *
 * Author: sunxy
 * Created: 2015-09-13 22:22
 * Since: 1.0
 */
public class GenericSagaSqlSchema implements SagaSqlSchema {

    protected final SchemaConfiguration schemaConfiguration;

    /**
     * 使用默认的配置初始化
     */
    public GenericSagaSqlSchema() {
        this(new SchemaConfiguration());
    }

    /**
     * 使用指定配置初始化
     *
     * @param schemaConfiguration
     */
    public GenericSagaSqlSchema(SchemaConfiguration schemaConfiguration) {
        this.schemaConfiguration = schemaConfiguration;
    }

    @Override
    public PreparedStatement sql_loadSaga(Connection connection, String sagaId) throws SQLException {
        final String sql = "SELECT serializedSaga, sagaType, revision FROM " + schemaConfiguration.sagaEntryTable() + " WHERE sagaId = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, sagaId);
        return preparedStatement;
    }

    @Override
    public PreparedStatement sql_removeAssocValue(Connection connection, String key, String value, String sagaType,
                                                  String sagaIdentifier) throws SQLException {
        final String sql = "DELETE FROM " + schemaConfiguration.assocValueEntryTable()
                + " WHERE associationKey = ? AND associationValue = ?"
                + " AND sagaType = ? AND sagaId = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, key);
        preparedStatement.setString(2, value);
        preparedStatement.setString(3, sagaType);
        preparedStatement.setString(4, sagaIdentifier);
        return preparedStatement;
    }

    @Override
    public PreparedStatement sql_storeAssocValue(Connection connection, String key, String value, String sagaType,
                                                 String sagaIdentifier) throws SQLException {
        final String sql = "INSERT INTO " + schemaConfiguration.assocValueEntryTable()
                + " (associationKey, associationValue, sagaType, sagaId)"
                + " VALUES(?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, key);
        preparedStatement.setString(2, value);
        preparedStatement.setString(3, sagaType);
        preparedStatement.setString(4, sagaIdentifier);
        return preparedStatement;
    }

    @Override
    public PreparedStatement sql_findAssocSagaIdentifiers(Connection connection, String key, String value,
                                                          String sagaType) throws SQLException {
        final String sql = "SELECT sagaId FROM " + schemaConfiguration.assocValueEntryTable()
                + " WHERE associationKey = ?"
                + " AND associationValue = ?"
                + " AND sagaType = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, key);
        preparedStatement.setString(2, value);
        preparedStatement.setString(3, sagaType);
        return preparedStatement;
    }

    @Override
    public PreparedStatement sql_deleteSagaEntry(Connection connection, String sagaIdentifier) throws SQLException {
        final String sql = "DELETE FROM " + schemaConfiguration.sagaEntryTable() + " WHERE sagaId = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, sagaIdentifier);
        return preparedStatement;
    }

    @Override
    public PreparedStatement sql_deleteAssociationEntries(Connection connection, String sagaIdentifier)
            throws SQLException {
        final String sql = "DELETE FROM " + schemaConfiguration.assocValueEntryTable() + " WHERE sagaId = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, sagaIdentifier);
        return preparedStatement;
    }

    @Override
    public PreparedStatement sql_updateSaga(Connection connection, String sagaIdentifier, byte[] serializedSaga,
                                            String sagaType, String revision) throws SQLException {
        final String sql = "UPDATE " + schemaConfiguration.sagaEntryTable()
                + " SET serializedSaga = ?, revision = ? WHERE sagaId = ? AND sagaType = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setBytes(1, serializedSaga);
        preparedStatement.setString(2, revision);
        preparedStatement.setString(3, sagaIdentifier);
        preparedStatement.setString(4, sagaType);
        return preparedStatement;
    }

    @Override
    public PreparedStatement sql_storeSaga(Connection connection, String sagaIdentifier, String revision,
                                           String sagaType,
                                           byte[] serializedSaga) throws SQLException {
        final String sql = "INSERT INTO " + schemaConfiguration.sagaEntryTable() + "(sagaId, revision, sagaType, serializedSaga) VALUES(?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, sagaIdentifier);
        preparedStatement.setString(2, revision);
        preparedStatement.setString(3, sagaType);
        preparedStatement.setBytes(4, serializedSaga);
        return preparedStatement;
    }

    @Override
    public PreparedStatement sql_createTableAssocValueEntry(Connection conn) throws SQLException {
        final String sql = "create table " + schemaConfiguration.assocValueEntryTable() + " (\n" +
                "        id int not null AUTO_GENERATED,\n" +
                "        associationKey varchar(255),\n" +
                "        associationValue varchar(255),\n" +
                "        sagaId varchar(255),\n" +
                "        sagaType varchar(255),\n" +
                "        primary key (id)\n" +
                "    );\n";
        return conn.prepareStatement(sql);
    }

    @Override
    public PreparedStatement sql_createTableSagaEntry(Connection conn) throws SQLException {
        return conn.prepareStatement("create table " + schemaConfiguration.sagaEntryTable() + " (\n" +
                "        sagaId varchar(255) not null,\n" +
                "        revision varchar(255),\n" +
                "        sagaType varchar(255),\n" +
                "        serializedSaga blob,\n" +
                "        primary key (sagaId)\n" +
                "    );");
    }

    @Override
    public SerializedObject<byte[]> readSerializedSaga(ResultSet resultSet) throws SQLException {
        return new SimpleSerializedObject<>(resultSet.getBytes(1), byte[].class,
                resultSet.getString(2),
                resultSet.getString(3));
    }

    @Override
    public PreparedStatement sql_findAssociationValues(Connection connection, String sagaIdntifier, String sagaType) throws SQLException {
        final String sql = "SELECT associationKey,  associationValue FROM " + schemaConfiguration.assocValueEntryTable()
                + " WHERE sagaId = ?"
                + " AND sagaType = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, sagaIdntifier);
        preparedStatement.setString(2, sagaType);
        return preparedStatement;
    }
}
