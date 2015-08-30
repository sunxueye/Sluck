package org.sluckframework.implement.eventstore.jdbc;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.jdbc.ConnectionProvider;
import org.sluckframework.common.jdbc.DataSourceConnectionProvider;
import org.sluckframework.common.jdbc.PersistenceExceptionResolver;
import org.sluckframework.common.jdbc.UnitOfWorkAwareConnectionProviderWrapper;
import org.sluckframework.common.serializer.EventSerializer;
import org.sluckframework.common.serializer.SerializedAggregateEventData;
import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.common.serializer.json.JacksonSerializer;
import org.sluckframework.common.util.IOUtils;
import org.sluckframework.cqrs.upcasting.SimpleUpcasterChain;
import org.sluckframework.cqrs.upcasting.UpcasterAware;
import org.sluckframework.cqrs.upcasting.UpcasterChain;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.aggregate.GenericAggregateEvent;
import org.sluckframework.domain.event.eventstore.EventStreamNotFoundException;
import org.sluckframework.domain.event.eventstore.SnapshotEventStore;
import org.sluckframework.domain.event.eventstore.query.EventStoreQueryManagement;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.ConcurrencyException;

import static org.sluckframework.domain.identifier.IdentifierValidator.validateIdentifier;


/**
 * jdbc 实现的 聚合事件仓储，真实的领域事件被序列化 后存储，其他的字段是为了给次聚合的事件建立索引，默认使用json来序列化
 * 
 * @author sunxy
 * @time 2015年8月29日 下午5:40:11
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class JdbcEventStore implements SnapshotEventStore, EventStoreQueryManagement, UpcasterAware{
	
	private static final Logger logger = LoggerFactory.getLogger(JdbcEventStore.class);

    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int DEFAULT_MAX_SNAPSHOTS_ARCHIVED = 1;

    private final EventSerializer serializer;
    private final EventEntryStore<?> eventEntryStore;
    private final JdbcCriteriaBuilder criteriaBuilder = new JdbcCriteriaBuilder();

    private int batchSize = DEFAULT_BATCH_SIZE;
    private UpcasterChain upcasterChain = SimpleUpcasterChain.EMPTY;
    private int maxSnapshotsArchived = DEFAULT_MAX_SNAPSHOTS_ARCHIVED;
    private PersistenceExceptionResolver persistenceExceptionResolver;

    /**
     * 使用真正存储的 eventEntryStore 和 serializer 初始化
     *
     * @param eventEntryStore The EventEntryStore that stores entries 
     * @param serializer      The serializer to serialize events 
     */
    public JdbcEventStore(EventEntryStore<?> eventEntryStore, Serializer serializer) {
        Assert.notNull(serializer, "serializer may not be null");
        Assert.notNull(eventEntryStore, "eventEntryStore may not be null");
        this.persistenceExceptionResolver = new JdbcSQLErrorCodesResolver();
        this.serializer = new EventSerializer(serializer);
        this.eventEntryStore = eventEntryStore;
    }

    /**
     * 使用指定的 eventEntryStore 和 jackjsonSerializer 初始化
     *
     * @param eventEntryStore The instance providing persistence logic for Domain Event entries
     */
    public JdbcEventStore(EventEntryStore<?> eventEntryStore) {
        this(eventEntryStore, new JacksonSerializer());
    }

    /**
     * 使用指定的 cp 和默认的 JacksonSerializer 初始化
     *
     * @param connectionProvider The connection provider to obtain connections from
     */
	public JdbcEventStore(ConnectionProvider connectionProvider) {
        this(new DefaultEventEntryStore(connectionProvider), new JacksonSerializer());
    }

    /**
     * 使用 数据源 初始化
     *
     * @param dataSource The DataSource to obtain connections from
     */
	public JdbcEventStore(DataSource dataSource) {
        this(new DefaultEventEntryStore(
                     new UnitOfWorkAwareConnectionProviderWrapper(new DataSourceConnectionProvider(dataSource))),
             new JacksonSerializer()
        );
    }

    @SuppressWarnings({ "unchecked"})
    @Override
    public void appendEvents(String type, AggregateEventStream events) {
        AggregateEvent event = null;
        try {
            while (events.hasNext()) {
                event = events.next();
                validateIdentifier(event.getAggregateIdentifier().getClass());
                final Class dataType = eventEntryStore.getDataType();
                SerializedObject serializedPayload = serializer.serializePayload(event, dataType);
                eventEntryStore.persistEvent(type, event, serializedPayload);
            }
        } catch (RuntimeException exception) {
            if (persistenceExceptionResolver != null
                    && persistenceExceptionResolver.isDuplicateKeyViolation(exception)) {
                //noinspection ConstantConditions
                throw new ConcurrencyException(
                        String.format("Concurrent modification detected for Aggregate identifier [%s], sequence: [%s]",
                                      event.getAggregateIdentifier(),
                                      event.getSequenceNumber()),
                        exception
                );
            }
            throw exception;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public AggregateEventStream readEvents(String type, Identifier<?> identifier) {
        long snapshotSequenceNumber = -1;
        SerializedAggregateEventData lastSnapshotEvent = eventEntryStore.loadLastSnapshotEvent(type, identifier);
        AggregateEvent snapshotEvent = null;
        if (lastSnapshotEvent != null) {
            try {
                snapshotEvent = new GenericAggregateEvent(
                        identifier,
                        lastSnapshotEvent.getSequenceNumber(),
                        serializer.deserialize(lastSnapshotEvent.getPayload()));
                snapshotSequenceNumber = snapshotEvent.getSequenceNumber();
            } catch (RuntimeException ex) {
                logger.warn("Error while reading snapshot event entry. "
                                    + "Reconstructing aggregate on entire event stream. Caused by: {} {}",
                            ex.getClass().getName(),
                            ex.getMessage()
                );
            } catch (LinkageError error) {
                logger.warn("Error while reading snapshot event entry. "
                                    + "Reconstructing aggregate on entire event stream. Caused by: {} {}",
                            error.getClass().getName(),
                            error.getMessage()
                );
            }
        }

        Iterator<? extends SerializedAggregateEventData> entries =
                eventEntryStore.fetchAggregateStream(type, identifier, snapshotSequenceNumber + 1, batchSize);
        if (snapshotEvent == null && !entries.hasNext()) {
            IOUtils.closeQuietlyIfCloseable(entries);
            throw new EventStreamNotFoundException(type, identifier);
        }
        return new IteratorAggregateEventStream(snapshotEvent, entries, identifier, false);
    }

    @Override
    public AggregateEventStream readEvents(String type, DomainIdentifier identifier, long firstSequenceNumber) {
        return readEvents(type, identifier, firstSequenceNumber, Long.MAX_VALUE);
    }

    @Override
    public AggregateEventStream readEvents(String type, DomainIdentifier identifier, long firstSequenceNumber,
                                        long lastSequenceNumber) {
        int minimalBatchSize = (int) Math.min(batchSize, (lastSequenceNumber - firstSequenceNumber) + 2);
        Iterator<? extends SerializedDomainEventData> entries = eventEntryStore.fetchAggregateStream(type,
                                                                                                     identifier,
                                                                                                     firstSequenceNumber,
                                                                                                     minimalBatchSize);
        if (!entries.hasNext()) {
            throw new EventStreamNotFoundException(type, identifier);
        }
        return new IteratorAggregateEventStream(null, entries, identifier, lastSequenceNumber, false);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Upon appending a snapshot, this particular EventStore implementation also prunes snapshots which are considered
     * redundant because they fall outside of the range of maximum snapshots to archive.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void appendSnapshotEvent(String type, AggregateEvent snapshotEvent) {
        // Persist snapshot before pruning redundant archived ones, in order to prevent snapshot misses when reloading
        // an aggregate, which may occur when a READ_UNCOMMITTED transaction isolation level is used.
        final Class<?> dataType = eventEntryStore.getDataType();
        SerializedObject serializedPayload = serializer.serializePayload(snapshotEvent, dataType);
        SerializedObject serializedMetaData = serializer.serializeMetaData(snapshotEvent, dataType);
        try {
            eventEntryStore.persistSnapshot(type, snapshotEvent, serializedPayload, serializedMetaData);
        } catch (RuntimeException exception) {
            if (persistenceExceptionResolver != null
                    && persistenceExceptionResolver.isDuplicateKeyViolation(exception)) {
                //noinspection ConstantConditions
                throw new ConcurrencyException(
                        String.format("A snapshot for aggregate [%s] at sequence: [%s] was already inserted",
                                      snapshotEvent.getAggregateIdentifier(),
                                      snapshotEvent.getSequenceNumber()),
                        exception
                );
            }
            throw exception;
        }
        if (maxSnapshotsArchived > 0) {
            eventEntryStore.pruneSnapshots(type, snapshotEvent, maxSnapshotsArchived);
        }
    }

    @Override
    public void visitEvents(EventVisitor visitor) {
        doVisitEvents(visitor, null, Collections.emptyList());
    }

    @Override
    public void visitEvents(Criteria criteria, EventVisitor visitor) {
        StringBuilder sb = new StringBuilder();
        ParameterRegistry parameters = new ParameterRegistry();
        ((JdbcCriteria) criteria).parse("", sb, parameters);
        doVisitEvents(visitor, sb.toString(), parameters.getParameters());
    }

    @Override
    public CriteriaBuilder newCriteriaBuilder() {
        return criteriaBuilder;
    }

    private void doVisitEvents(EventVisitor visitor, String whereClause, List<Object> parameters) {
        Iterator<? extends SerializedDomainEventData> batch = eventEntryStore.fetchFiltered(whereClause,
                                                                                            parameters,
                                                                                            batchSize
        );
        AggregateEventStream eventStream = new IteratorAggregateEventStream(null, batch, null, true);
        try {
            while (eventStream.hasNext()) {
                visitor.doWithEvent(eventStream.next());
            }
        } finally {
            IOUtils.closeQuietlyIfCloseable(eventStream);
        }
    }

    /**
     * Sets the persistenceExceptionResolver that will help detect concurrency exceptions from the backing database.
     *
     * @param persistenceExceptionResolver the persistenceExceptionResolver that will help detect concurrency
     *                                     exceptions
     */
    public void setPersistenceExceptionResolver(PersistenceExceptionResolver persistenceExceptionResolver) {
        this.persistenceExceptionResolver = persistenceExceptionResolver;
    }

    /**
     * Sets the number of events that should be read at each database access. When more than this number of events must
     * be read to rebuild an aggregate's state, the events are read in batches of this size. Defaults to 100.
     * <p/>
     * Tip: if you use a snapshotter, make sure to choose snapshot trigger and batch size such that a single batch will
     * generally retrieve all events required to rebuild an aggregate's state.
     *
     * @param batchSize the number of events to read on each database access. Default to 100.
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void setUpcasterChain(UpcasterChain upcasterChain) {
        this.upcasterChain = upcasterChain;
    }

    /**
     * Sets the maximum number of snapshots to archive for an aggregate. The EventStore will keep at most this number
     * of
     * snapshots per aggregate.
     * <p/>
     * Defaults to {@value #DEFAULT_MAX_SNAPSHOTS_ARCHIVED}.
     *
     * @param maxSnapshotsArchived The maximum number of snapshots to archive for an aggregate. A value less than 1
     *                             disables pruning of snapshots.
     */
    public void setMaxSnapshotsArchived(int maxSnapshotsArchived) {
        this.maxSnapshotsArchived = maxSnapshotsArchived;
    }

    private final class IteratorAggregateEventStream implements AggregateEventStream, Closeable {

        private Iterator<AggregateEvent> currentBatch;
        private AggregateEvent next;
        private final Iterator<? extends SerializedAggregateEventData> iterator;
        private final Identifier<?> aggregateIdentifier;
        private final long lastSequenceNumber;
        private final boolean skipUnknownTypes;

        public IteratorAggregateEventStream(AggregateEvent snapshotEvent,
                                         Iterator<? extends SerializedAggregateEventData> iterator,
                                         Identifier<?> aggregateIdentifier, boolean skipUnknownTypes) {
            this(snapshotEvent, iterator, aggregateIdentifier, Long.MAX_VALUE, skipUnknownTypes);
        }

        public IteratorAggregateEventStream(AggregateEvent snapshotEvent,
                                         Iterator<? extends SerializedAggregateEventData> iterator,
                                         Identifier<?> aggregateIdentifier, long lastSequenceNumber,
                                         boolean skipUnknownTypes) {
            this.aggregateIdentifier = aggregateIdentifier;
            this.lastSequenceNumber = lastSequenceNumber;
            this.skipUnknownTypes = skipUnknownTypes;
            if (snapshotEvent != null) {
                currentBatch = Collections.singletonList(snapshotEvent).iterator();
            } else {
                currentBatch = Collections.<AggregateEvent>emptyList().iterator();
            }
            this.iterator = iterator;
            initializeNextItem();
        }


        @Override
        public boolean hasNext() {
            return next != null && next.getSequenceNumber() <= lastSequenceNumber;
        }

        @Override
        public AggregateEvent next() {
            AggregateEvent current = next;
            initializeNextItem();
            return current;
        }

        private void initializeNextItem() {
            while (!currentBatch.hasNext() && iterator.hasNext()) {
                final SerializedAggregateEventData entry = iterator.next();
                currentBatch = upcastAndDeserialize(entry, aggregateIdentifier, serializer, upcasterChain,
                                                    skipUnknownTypes)
                        .iterator();
            }
            next = currentBatch.hasNext() ? currentBatch.next() : null;
        }

        @Override
        public AggregateEvent peek() {
            return next;
        }

        @Override
        public void close() throws IOException {
            IOUtils.closeIfCloseable(iterator);
        }
    }

}
