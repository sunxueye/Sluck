package org.sluckframework.cqrs.saga.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 为了 方便 客户端自定义 表名
 *
 * Author: sunxy
 * Created: 2015-09-13 22:41
 * Since: 1.0
 */
public class PostgresSagaSqlSchema extends GenericSagaSqlSchema{

    public PostgresSagaSqlSchema() {
    }

    public PostgresSagaSqlSchema(SchemaConfiguration schemaConfiguration) {
        super(schemaConfiguration);
    }

    @Override
    public PreparedStatement sql_createTableAssocValueEntry(Connection conn) throws SQLException {
        final String sql = "create table " + schemaConfiguration.assocValueEntryTable() + " (\n" +
                "        id bigserial not null,\n" +
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
                "        serializedSaga bytea,\n" +
                "        primary key (sagaId)\n" +
                "    );");
    }

}
