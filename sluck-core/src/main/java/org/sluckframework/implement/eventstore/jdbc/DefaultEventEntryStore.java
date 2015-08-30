package org.sluckframework.implement.eventstore.jdbc;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.sluckframework.common.jdbc.ConnectionProvider;
import org.sluckframework.common.jdbc.DataSourceConnectionProvider;
import org.sluckframework.common.jdbc.UnitOfWorkAwareConnectionProviderWrapper;
import org.sluckframework.common.serializer.SerializedAggregateEventData;
import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.util.IOUtils;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.eventstore.EventStoreException;
import org.sluckframework.domain.identifier.Identifier;

import static org.sluckframework.common.jdbc.JdbcUtils.closeQuietly;

/**
 * 真正 的 聚合事件和快照事件 存储 执行者 
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:15:18
 * @since 1.0
 */
public class DefaultEventEntryStore<T> implements EventEntryStore<T> {

    private final ConnectionProvider connectionProvider;

    private final EventSqlSchema<T> sqlSchema;

    /**
     * 使用指定的 数据源 和 sqlSchema 初始化
     * 
     * @param dataSource The data source used to create connections
     * @param sqlSchema  The SQL Definitions
     */
    public DefaultEventEntryStore(DataSource dataSource, EventSqlSchema<T> sqlSchema) {
        this(new UnitOfWorkAwareConnectionProviderWrapper(new DataSourceConnectionProvider(dataSource)), sqlSchema);
    }

    /**
     * 使用指定的 cp 和 sqlSchema 初始化
     *
     * @param connectionProvider Used to obtain connections
     * @param sqlSchema          The SQL Definitions
     */
    public DefaultEventEntryStore(ConnectionProvider connectionProvider, EventSqlSchema<T> sqlSchema) {
        this.connectionProvider = connectionProvider;
        this.sqlSchema = sqlSchema;
    }

    /**
     * 使用指定的 cp 和默认的 sqlSchema 初始化
     * @param connectionProvider Used to obtain connections
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DefaultEventEntryStore(ConnectionProvider connectionProvider) {
        this(connectionProvider, new GenericEventSqlSchema());
    }

    @Override
    public SerializedAggregateEventData<T> loadLastSnapshotEvent(String aggregateType, Identifier<?> identifier) {
        ResultSet result = null;
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            preparedStatement = sqlSchema.sql_loadLastSnapshot(connection, identifier, aggregateType);
            result = preparedStatement.executeQuery();
            if (result.next()) {
                return sqlSchema.createSerializedDomainEventData(result);
            }
            return null;
        } catch (SQLException e) {
            throw new EventStoreException("Exception while attempting to load last snapshot event of "
                                                  + aggregateType + "/" + identifier, e);
        } finally {
            closeQuietly(result);
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }
    }

    @Override
    public Iterator<SerializedAggregateEventData<T>> fetchFiltered(String whereClause, List<Object> parameters,
                                                                int batchSize) {
        try {
            Connection connection = connectionProvider.getConnection();
            return new ConnectionResourceManagingIterator<T>(
                    new FilteredBatchingIterator<T>(whereClause, parameters, batchSize, sqlSchema, connection),
                    connection);
        } catch (SQLException e) {
            throw new EventStoreException("Exception while attempting to read from the Event Store database", e);
        }
        // we don't want to close the connection here. The ConnectionResourceManagingIterator will close the connection
        // when it finishes iterating the results.
    }


    @SuppressWarnings("rawtypes")
	@Override
    public void persistSnapshot(String aggregateType, AggregateEvent snapshotEvent,
                                SerializedObject<T> serializedPayload) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            preparedStatement = sqlSchema.sql_insertSnapshotEventEntry(connection,
                                                                       snapshotEvent.getIdentifier().toString(),
                                                                       snapshotEvent.getAggregateIdentifier()
                                                                                    .getIdentifier().toString(),
                                                                       snapshotEvent.getSequenceNumber(),
                                                                       snapshotEvent.occurredOn(),
                                                                       serializedPayload.getType().getName(),
                                                                       serializedPayload.getType().getRevision(),
                                                                       serializedPayload.getData(),
                                                                       aggregateType);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new EventStoreException("Exception while attempting to persist a snapshot", e);
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void persistEvent(String aggregateType, AggregateEvent event, SerializedObject<T> serializedPayload) {

        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            preparedStatement = sqlSchema.sql_insertDomainEventEntry(connection,
                                                                     event.getIdentifier().toString(),
                                                                     event.getAggregateIdentifier().getIdentifier().toString(),
                                                                     event.getSequenceNumber(),
                                                                     event.occurredOn(),
                                                                     serializedPayload.getType().getName(),
                                                                     serializedPayload.getType().getRevision(),
                                                                     serializedPayload.getData(),
                                                                     aggregateType
            );
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new EventStoreException("Exception occurred while attempting to persist an event", e);
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(connection);
        }
    }


    @SuppressWarnings("rawtypes")
	@Override
    public void pruneSnapshots(String type, AggregateEvent mostRecentSnapshotEvent, int maxSnapshotsArchived) {
        Iterator<Long> redundantSnapshots = findRedundantSnapshots(type, mostRecentSnapshotEvent,
                                                                   maxSnapshotsArchived);
        if (redundantSnapshots.hasNext()) {
            long sequenceOfFirstSnapshotToPrune = redundantSnapshots.next();
            Connection connection = null;
            try {
                connection = connectionProvider.getConnection();
                executeUpdate(sqlSchema.sql_pruneSnapshots(connection,
                                                           type,
                                                           mostRecentSnapshotEvent.getAggregateIdentifier(),
                                                           sequenceOfFirstSnapshotToPrune), "prune snapshots");
            } catch (SQLException e) {
                throw new EventStoreException("An exception occurred while attempting to prune snapshots", e);
            } finally {
                closeQuietly(connection);
            }
        }
    }

    @Override
    public Class<T> getDataType() {
        return sqlSchema.getDataType();
    }

    /**
     * Finds the first of redundant snapshots, returned as an iterator for convenience purposes.
     *
     * @param type                 the type of the aggregate for which to find redundant snapshots
     * @param snapshotEvent        the last appended snapshot event
     * @param maxSnapshotsArchived the number of snapshots that may remain archived
     * @return an iterator over the snapshots found
     */
    @SuppressWarnings({ "rawtypes"})
	private Iterator<Long> findRedundantSnapshots(String type, AggregateEvent snapshotEvent,
                                                  int maxSnapshotsArchived) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            statement = sqlSchema.sql_findSnapshotSequenceNumbers(connection,
                                                                  type,
                                                                  snapshotEvent.getAggregateIdentifier());
            resultSet = statement.executeQuery();
            //noinspection StatementWithEmptyBody
            while (maxSnapshotsArchived-- > 0 && resultSet.next()) { // NOSONAR
                // ignore
            }
            List<Long> result = new ArrayList<Long>();
            while (resultSet.next()) {
                result.add(resultSet.getLong(1));
            }
            resultSet.close();
            return result.iterator();
        } catch (SQLException e) {
            throw new EventStoreException("Exception ", e);
        } finally {
            closeQuietly(resultSet);
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    @Override
    public Iterator<SerializedAggregateEventData<T>> fetchAggregateStream(String aggregateType, Identifier<?> identifier,
                                                                    long firstSequenceNumber, int fetchSize) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = connectionProvider.getConnection();
            statement = sqlSchema.sql_fetchFromSequenceNumber(connection, aggregateType, identifier,
                                                              firstSequenceNumber);
            statement.setFetchSize(fetchSize);
            return new ConnectionResourceManagingIterator<T>(
                    new PreparedStatementIterator<T>(statement, sqlSchema),
                    connection);
        } catch (SQLException e) {
            closeQuietly(connection);
            closeQuietly(statement);
            throw new EventStoreException("Exception while attempting to read from an Aggregate Stream", e);
        }
    }

    private static class ConnectionResourceManagingIterator<T>
            implements Iterator<SerializedAggregateEventData<T>>, Closeable {

        private final Iterator<SerializedAggregateEventData<T>> inner;
        private final Connection connection;

		public ConnectionResourceManagingIterator(Iterator<SerializedAggregateEventData<T>> inner, Connection connection) {
            this.inner = inner;
            this.connection = connection;
        }

        @Override
        public boolean hasNext() {
            return inner.hasNext();
        }

        @Override
        public SerializedAggregateEventData<T> next() {
            return inner.next();
        }

        @Override
        public void remove() {
            inner.remove();
        }

        @Override
        public void close() throws IOException {
            IOUtils.closeQuietlyIfCloseable(inner);
            closeQuietly(connection);
        }
    }

    private static class FilteredBatchingIterator<T> implements Iterator<SerializedAggregateEventData<T>>, Closeable {

        private final Connection connection;
        private PreparedStatementIterator<T> currentBatch;
        private SerializedAggregateEventData<T> next;
        private SerializedAggregateEventData<T>  lastItem;
        private final String whereClause;
        private final List<Object> parameters;
        private final int batchSize;
        private final EventSqlSchema<T> sqlSchema;

        public FilteredBatchingIterator(
                String whereClause,
                List<Object> parameters,
                int batchSize,
                EventSqlSchema<T> sqlSchema,
                Connection connection) {
            this.whereClause = whereClause;
            this.parameters = parameters;
            this.batchSize = batchSize;
            this.connection = connection;
            this.sqlSchema = sqlSchema;

            this.currentBatch = fetchBatch();
            if (currentBatch.hasNext()) {
                next = currentBatch.next();
            }
        }

        private PreparedStatementIterator<T> fetchBatch() {
            LinkedList<Object> params = new LinkedList<Object>(parameters);
            String batchWhereClause = buildWhereClause(params);
            try {
                final PreparedStatement sql = sqlSchema.sql_getFetchAll(
                        connection,
                        batchWhereClause,
                        params.toArray());
                sql.setMaxRows(batchSize);
                return new PreparedStatementIterator<T>(sql, sqlSchema);
            } catch (SQLException e) {
                throw new EventStoreException("Exception occurred while attempting to execute prepared statement", e);
            }
        }

        private String buildWhereClause(List<Object> params) {
            if (lastItem == null && whereClause == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            if (lastItem != null) {
                sb.append("(")
                  .append("(e.timeStamp > ?)")
                  .append(" OR ")
                  .append("(e.timeStamp = ? AND e.sequenceNumber > ?)")
                  .append(" OR ")
                  .append("(e.timeStamp = ? AND e.sequenceNumber = ? AND e.aggregateIdentifier > ?)")
                  .append(")");
                Object dateTimeSql = sqlSchema.sql_dateTime(lastItem.getTimestamp());
                params.add(0, dateTimeSql);

                params.add(1, dateTimeSql);
                params.add(2, lastItem.getSequenceNumber());

                params.add(3, dateTimeSql);
                params.add(4, lastItem.getSequenceNumber());
                params.add(5, lastItem.getAggregateIdentifier());
            }
            if (whereClause != null && whereClause.length() > 0) {
                if (lastItem != null) {
                    sb.append(" AND (");
                }
                sb.append(whereClause);
                if (lastItem != null) {
                    sb.append(")");
                }
            }
            if (sb.length() > 0) {
                sb.insert(0, "WHERE ");
            }
            return sb.toString();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public SerializedAggregateEventData<T> next() {
        	SerializedAggregateEventData<T> current = next;
            lastItem = next;
            if (next != null && !currentBatch.hasNext() && currentBatch.readCount() >= batchSize) {
                IOUtils.closeQuietly(currentBatch);
                currentBatch = fetchBatch();
            }
            next = currentBatch.hasNext() ? currentBatch.next() : null;
            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Iterator is read-only");
        }

        @Override
        public void close() throws IOException {
            IOUtils.closeQuietly(currentBatch);
        }
    }

    private static class PreparedStatementIterator<T> implements Iterator<SerializedAggregateEventData<T>>, Closeable {

        private final PreparedStatement statement;
        private final ResultSetIterator<T> rsIterator;

        public PreparedStatementIterator(PreparedStatement statement, EventSqlSchema<T> sqlSchema) {
            this.statement = statement;
            try {
                ResultSet resultSet = statement.executeQuery();
                rsIterator = new ResultSetIterator<T>(resultSet, sqlSchema);
            } catch (SQLException e) {
                throw new EventStoreException("Exception occurred while attempting to execute query on statement", e);
            }
        }

        public int readCount() {
            return rsIterator.readCount();
        }

        @Override
        public boolean hasNext() {
            return rsIterator.hasNext();
        }

        @Override
        public SerializedAggregateEventData<T> next() {
            return rsIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Iterator is read-only");
        }

        @Override
        public void close() throws IOException {
            rsIterator.close();
            closeQuietly(statement);
        }
    }

    private static class ResultSetIterator<T> implements Iterator<SerializedAggregateEventData<T>>, Closeable {

        private final ResultSet rs;
        private final EventSqlSchema<T> sqlSchema;
        private boolean hasCalledNext = false;
        private boolean hasNext;
        private int counter = 0;

        public ResultSetIterator(ResultSet resultSet, EventSqlSchema<T> sqlSchema) {
            this.rs = resultSet;
            this.sqlSchema = sqlSchema;
        }

        @Override
        public boolean hasNext() {
            try {
                establishNext();
                return hasNext;
            } catch (SQLException e) {
                throw new EventStoreException("Exception occurred while attempting to fetch data from ResultSet", e);
            }
        }

        private void establishNext() throws SQLException {
            if (!hasCalledNext) {
                hasNext = rs.next();
                hasCalledNext = true;
            }
        }

        @Override
        public SerializedAggregateEventData<T> next() {
            try {
                establishNext();
                if (hasNext) {
                    counter++;
                }
                return sqlSchema.createSerializedDomainEventData(rs);
            } catch (SQLException e) {
                throw new EventStoreException("Exception occurred while attempting to read next event from ResultSet",
                                              e);
            } finally {
                hasCalledNext = false;
            }
        }

        public int readCount() {
            return counter;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Iterator is read-only");
        }

        @Override
        public void close() throws IOException {
            closeQuietly(rs);
        }
    }

    private int executeUpdate(PreparedStatement preparedStatement, String description) {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new EventStoreException("Exception occurred while attempting to " + description, e);
        } finally {
            closeQuietly(preparedStatement);
        }
    }

    /**
     * Performs the DDL queries to create the schema necessary for this EventEntryStore implementation.
     *
     * @throws SQLException when an error occurs executing SQL statements
     */
    public void createSchema() throws SQLException {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            executeUpdate(sqlSchema.sql_createDomainEventEntryTable(connection), "create domain event entry table");
            executeUpdate(sqlSchema.sql_createSnapshotEventEntryTable(connection), "create snapshot entry table");
        } finally {
            closeQuietly(connection);
        }
    }

}
