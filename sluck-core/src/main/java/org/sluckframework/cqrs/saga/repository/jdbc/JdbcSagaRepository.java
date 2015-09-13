package org.sluckframework.cqrs.saga.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.jdbc.ConnectionProvider;
import org.sluckframework.common.jdbc.DataSourceConnectionProvider;
import org.sluckframework.common.jdbc.UnitOfWorkAwareConnectionProviderWrapper;
import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.common.serializer.json.JacksonSerializer;
import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.ResourceInjector;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.cqrs.saga.SagaStorageException;
import org.sluckframework.cqrs.saga.repository.AbstractSagaRepository;
import org.sluckframework.cqrs.saga.repository.jpa.SagaEntry;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import static org.sluckframework.common.jdbc.JdbcUtils.closeQuietly;

/**
 * jdbc saga仓储的实现
 *
 * Author: sunxy
 * Created: 2015-09-13 22:19
 * Since: 1.0
 */
public class JdbcSagaRepository extends AbstractSagaRepository {

    private static final Logger logger = LoggerFactory.getLogger(JdbcSagaRepository.class);

    private ResourceInjector injector;
    private Serializer serializer;
    private final ConnectionProvider connectionProvider;

    private final SagaSqlSchema sqldef;

    /**
     * 使用指定的数据连接 和 默认配置的 sqlSchema初始化
     *
     * @param connectionProvider The data source to obtain connections from
     */
    public JdbcSagaRepository(ConnectionProvider connectionProvider) {
        this(connectionProvider, new GenericSagaSqlSchema());
    }

    /**
     * 使用指定的数据连接 和 指定配置的 sqlSchema初始化
     *
     * @param dataSource The data source to obtain connections from
     * @param sqldef     The definition of SQL operations to execute
     */
    public JdbcSagaRepository(DataSource dataSource, SagaSqlSchema sqldef) {
        this(new UnitOfWorkAwareConnectionProviderWrapper(new DataSourceConnectionProvider(dataSource)), sqldef);
    }

    /**
     * 使用指定的 配置 和 jackJson serializer 初始化
     *
     * @param connectionProvider The provider to obtain connections from
     * @param sqldef             The definition of SQL operations to execute
     */
    public JdbcSagaRepository(ConnectionProvider connectionProvider, SagaSqlSchema sqldef) {
        this(connectionProvider, sqldef, new JacksonSerializer());
    }

    /**
     * 使用指定的 配置 初始化
     *
     * @param connectionProvider The provider to obtain connections from
     * @param sqldef             The definition of SQL operations to execute
     * @param serializer         The serializer to serialize and deserialize Saga instances with
     */
    public JdbcSagaRepository(ConnectionProvider connectionProvider,
                              SagaSqlSchema sqldef, Serializer serializer) {
        this.connectionProvider = connectionProvider;
        this.sqldef = sqldef;
        this.serializer = serializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Saga load(String sagaId) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            statement = sqldef.sql_loadSaga(conn, sagaId);
            resultSet = statement.executeQuery();

            SerializedObject<?> serializedSaga = null;
            if (resultSet.next()) {
                serializedSaga = sqldef.readSerializedSaga(resultSet);
            }
            if (serializedSaga == null) {
                return null;
            }
            Saga loadedSaga = serializer.deserialize(serializedSaga);
            if (injector != null) {
                injector.injectResources(loadedSaga);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded saga id [{}] of type [{}]", sagaId, loadedSaga.getClass().getName());
            }
            return loadedSaga;
        } catch (SQLException e) {
            throw new SagaStorageException("Exception while loading a Saga", e);
        } finally {
            closeQuietly(statement);
            closeQuietly(resultSet);
            closeQuietly(conn);
        }
    }


    @SuppressWarnings({"unchecked"})
    @Override
    protected void removeAssociationValue(AssociationValue associationValue, String sagaType, String sagaIdentifier) {
        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            PreparedStatement preparedStatement = sqldef.sql_removeAssocValue(conn,
                    associationValue.getKey(),
                    associationValue.getValue(),
                    sagaType,
                    sagaIdentifier);
            int updateCount = preparedStatement.executeUpdate();

            if (updateCount == 0 && logger.isWarnEnabled()) {
                logger.warn("Wanted to remove association value, but it was already gone: sagaId= {}, key={}, value={}",
                        sagaIdentifier,
                        associationValue.getKey(),
                        associationValue.getValue());
            }
        } catch (SQLException e) {
            throw new SagaStorageException("Exception occurred while attempting to remove an AssociationValue", e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    protected String typeOf(Class<? extends Saga> sagaClass) {
        return serializer.typeForClass(sagaClass).getName();
    }

    @Override
    protected void storeAssociationValue(AssociationValue associationValue, String sagaType, String sagaIdentifier) {
        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            statement = sqldef.sql_storeAssocValue(conn,
                    associationValue.getKey(),
                    associationValue.getValue(),
                    sagaType,
                    sagaIdentifier);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SagaStorageException("Exception while storing an association value", e);
        } finally {
            closeQuietly(statement);
            closeQuietly(conn);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<String> findAssociatedSagaIdentifiers(Class<? extends Saga> type, AssociationValue associationValue) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            statement = sqldef.sql_findAssocSagaIdentifiers(conn, associationValue.getKey(),
                    associationValue.getValue(), typeOf(type));
            resultSet = statement.executeQuery();
            Set<String> result = new TreeSet<String>();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
            return result;
        } catch (SQLException e) {
            throw new SagaStorageException("Exception while reading saga associations", e);
        } finally {
            closeQuietly(statement);
            closeQuietly(resultSet);
            closeQuietly(conn);
        }
    }

    @Override
    protected void deleteSaga(Saga saga) {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            statement1 = sqldef.sql_deleteAssociationEntries(conn, saga.getSagaIdentifier());
            statement2 = sqldef.sql_deleteSagaEntry(conn, saga.getSagaIdentifier());
            statement1.executeUpdate();
            statement2.executeUpdate();
        } catch (SQLException e) {
            throw new SagaStorageException("Exception occurred while attempting to delete a saga entry", e);
        } finally {
            closeQuietly(statement1);
            closeQuietly(statement2);
            closeQuietly(conn);
        }
    }


    @Override
    protected void updateSaga(Saga saga) {
        SagaEntry entry = new SagaEntry(saga, serializer);
        if (logger.isDebugEnabled()) {
            logger.debug("Updating saga id {} as {}", saga.getSagaIdentifier(), new String(entry.getSerializedSaga(),
                    Charset.forName("UTF-8")));
        }

        int updateCount = 0;
        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            statement = sqldef.sql_updateSaga(conn,
                    entry.getSagaId(),
                    entry.getSerializedSaga(),
                    entry.getSagaType(),
                    entry.getRevision()
            );
            updateCount = statement.executeUpdate();
        } catch (SQLException e) {
            throw new SagaStorageException("Exception occurred while attempting to update a saga", e);
        } finally {
            closeQuietly(statement);
            closeQuietly(conn);
        }

        if (updateCount == 0) {
            logger.warn("Expected to be able to update a Saga instance, but no rows were found. Inserting instead.");
            storeSaga(saga);
        }
    }

    @Override
    protected void storeSaga(Saga saga) {
        SagaEntry entry = new SagaEntry(saga, serializer);
        if (logger.isDebugEnabled()) {
            logger.debug("Storing saga id {} as {}", saga.getSagaIdentifier(), new String(entry.getSerializedSaga(),
                    Charset.forName("UTF-8")));
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = connectionProvider.getConnection();
            statement = sqldef.sql_storeSaga(conn, entry.getSagaId(), entry.getRevision(), entry.getSagaType(),
                    entry.getSerializedSaga());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SagaStorageException("Exception occurred while attempting to store a Saga Entry", e);
        } finally {
            closeQuietly(statement);
            closeQuietly(conn);
        }
    }

    /**
     * 为saga配置资源注入器
     *
     * @param resourceInjector The resource injector
     */
    public void setResourceInjector(ResourceInjector resourceInjector) {
        this.injector = resourceInjector;
    }

    /**
     * 设置序列化对象
     *
     * @param serializer the Serializer instance to serialize Sagas with
     */
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 建表 schema saga and assocValue table
     * Creates the SQL Schema required to store Sagas and their associations,.
     *
     * @throws SQLException When an error occurs preparing of executing the required statements
     */
    public void createSchema() throws SQLException {
        final Connection connection = connectionProvider.getConnection();
        try {
            sqldef.sql_createTableSagaEntry(connection).executeUpdate();
            sqldef.sql_createTableAssocValueEntry(connection).executeUpdate();
        } finally {
            closeQuietly(connection);
        }
    }
}
