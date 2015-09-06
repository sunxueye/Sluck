package org.sluckframework.cqrs.eventsourcing;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.util.IOUtils;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.AggregateNotFoundException;
import org.sluckframework.domain.repository.lock.LockManager;
import org.sluckframework.domain.repository.lock.LockingRepository;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.event.eventstore.EventStreamNotFoundException;
import org.sluckframework.cqrs.eventsourcing.ConflictResolver;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListenerAdapter;


/**
 * es 类型的 仓储 ，内部实现 将 新的事件发布到 eventBUs， 存储到 AggregateEventStore
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:46:41	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class EventSourcingRepository<T extends EventSourcedAggregateRoot<ID>, ID extends Identifier<?>>
		extends LockingRepository<T, ID> {

    private final AggregateEventStore AggregateEventStore;
    private ConflictResolver conflictResolver;
    private final Deque<EventStreamDecorator> eventStreamDecorators = new ArrayDeque<EventStreamDecorator>();
    private final AggregateFactory<T> aggregateFactory;

    /**
     * 使用指定的 聚合类型 和 聚合事件仓储 初始化
     *
     * @param aggregateType The type of aggregate stored in this repository
     * @param AggregateEventStore    The event store that holds the event streams for this repository
     */
    public EventSourcingRepository(final Class<T> aggregateType, AggregateEventStore AggregateEventStore) {
        this(new GenericAggregateFactory<T>(aggregateType), AggregateEventStore);
    }

    /**
     * 使用指定的 聚合工厂 和 聚合 事件仓储 初始化
     *
     * @param aggregateFactory The factory for new aggregate instances
     * @param AggregateEventStore       The event store that holds the event streams for this repository
     */
    public EventSourcingRepository(final AggregateFactory<T> aggregateFactory, AggregateEventStore AggregateEventStore) {
        super(aggregateFactory.getAggregateType());
        Assert.notNull(AggregateEventStore, "AggregateEventStore may not be null");
        this.aggregateFactory = aggregateFactory;
        this.AggregateEventStore = AggregateEventStore;
    }

    /**
     * 使用指定的 锁 机制 初始化
     *
     * @param aggregateFactory The factory for new aggregate instances
     * @param AggregateEventStore       The event store that holds the event streams for this repository
     * @param lockManager      the locking strategy to apply to this repository
     */
    public EventSourcingRepository(AggregateFactory<T> aggregateFactory, AggregateEventStore AggregateEventStore,
                                   LockManager lockManager) {
        super(aggregateFactory.getAggregateType(), lockManager);
        Assert.notNull(AggregateEventStore, "AggregateEventStore may not be null");
        this.AggregateEventStore = AggregateEventStore;
        this.aggregateFactory = aggregateFactory;
    }

    /**
     * 使用指定 属性初始化
     *
     * @param aggregateType The type of aggregate to store in this repository
     * @param AggregateEventStore    The event store that holds the event streams for this repository
     * @param lockManager   the locking strategy to apply to this
     */
    public EventSourcingRepository(final Class<T> aggregateType, AggregateEventStore AggregateEventStore,
                                   final LockManager lockManager) {
        this(new GenericAggregateFactory<T>(aggregateType), AggregateEventStore, lockManager);
    }

    /**
     * 执行真正的 保存 事件操作
     *
     * @param aggregate the aggregate to store
     */
    @Override
    protected void doSaveWithLock(T aggregate) {
        AggregateEventStream eventStream = aggregate.getUncommittedEvents();
        try {
            Iterator<EventStreamDecorator> iterator = eventStreamDecorators.descendingIterator();
            while (iterator.hasNext()) {
                eventStream = iterator.next().decorateForAppend(getTypeIdentifier(), aggregate, eventStream);
            }
            AggregateEventStore.appendEvents(getTypeIdentifier(), eventStream);
        } finally {
            IOUtils.closeQuietlyIfCloseable(eventStream);
        }
    }

    @Override
    protected void doDeleteWithLock(T aggregate) {
        doSaveWithLock(aggregate);
    }

    /**
     * 根据聚合标识符和 指定 版本加载 聚合
     *
     * @param aggregateIdentifier the identifier of the aggregate to load
     * @param expectedVersion     The expected version of the loaded aggregate
     * @return the fully initialized aggregate
     *
     */
    @Override
    protected T doLoad(ID aggregateIdentifier, final Long expectedVersion) {
    	AggregateEventStream events = null;
    	AggregateEventStream originalStream = null;
        try {
            try {
                events = AggregateEventStore.readEvents(getTypeIdentifier(), aggregateIdentifier);
            } catch (EventStreamNotFoundException e) {
                throw new AggregateNotFoundException(aggregateIdentifier, "The aggregate was not found", e);
            }
            originalStream = events;
            for (EventStreamDecorator decorator : eventStreamDecorators) {
                events = decorator.decorateForRead(getTypeIdentifier(), aggregateIdentifier, events);
            }

            final T aggregate = aggregateFactory.createAggregate(aggregateIdentifier, events.peek());
            List<AggregateEvent> unseenEvents = new ArrayList<AggregateEvent>();
            aggregate.initializeState(new CapturingEventStream(events, unseenEvents, expectedVersion));
            if (aggregate.isDeleted()) {
                throw new AggregateDeletedException(aggregateIdentifier);
            }
            CurrentUnitOfWork.get().registerListener(new ConflictResolvingListener(aggregate, unseenEvents));

            return aggregate;
        } finally {
            IOUtils.closeQuietlyIfCloseable(events);
            // if a decorator doesn't implement closeable, we still want to be sure we close the original stream
            IOUtils.closeQuietlyIfCloseable(originalStream);
        }
    }

    public AggregateFactory<T> getAggregateFactory() {
        return aggregateFactory;
    }

    /**
     * 解决 聚合 的事件 并发 竞争
     *
     * @param aggregate    The aggregate containing the potential conflicts
     * @param unseenEvents The events that have been concurrently applied
     */
    protected void resolveConflicts(T aggregate, AggregateEventStream unseenEvents) {
        CurrentUnitOfWork.get().registerListener(new ConflictResolvingListener(aggregate, asList(unseenEvents)));
    }

    private List<AggregateEvent> asList(AggregateEventStream AggregateEventStream) {
        List<AggregateEvent> unseenEvents = new ArrayList<AggregateEvent>();
        while (AggregateEventStream.hasNext()) {
            unseenEvents.add(AggregateEventStream.next());
        }
        return unseenEvents;
    }

    public String getTypeIdentifier() {
        if (aggregateFactory == null) {
            throw new IllegalStateException("Either an aggregate factory must be configured (recommended), "
                                                    + "or the getTypeIdentifier() method must be overridden.");
        }
        return aggregateFactory.getTypeIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateOnLoad(T aggregate, Long expectedVersion) {
        if (conflictResolver == null) {
            super.validateOnLoad(aggregate, expectedVersion);
        }
    }

    /**
     * 设置 事件流装饰器
     *
     * @param eventProcessors The processors to that will process events in the AggregateEventStream
     */
    public void setEventStreamDecorators(List<? extends EventStreamDecorator> eventProcessors) {
        this.eventStreamDecorators.addAll(eventProcessors);
    }

    /**
     * 设置 仓储的 聚合快照 触发器
     *
     * @param snapshotterTrigger the snapshotter trigger for this repository.
     */
    public void setSnapshotterTrigger(SnapshotterTrigger snapshotterTrigger) {
        this.eventStreamDecorators.add(snapshotterTrigger);
    }

    /**
     * 设置 仓储 并发竞争 协调器
     *
     * @param conflictResolver The conflict resolver to use for this repository
     */
    public void setConflictResolver(ConflictResolver conflictResolver) {
        this.conflictResolver = conflictResolver;
    }

    private final class ConflictResolvingListener extends UnitOfWorkListenerAdapter {

        private final T aggregate;
		private final List<AggregateEvent> unseenEvents;

        private ConflictResolvingListener(T aggregate, List<AggregateEvent> unseenEvents) {
            this.aggregate = aggregate;
            this.unseenEvents = unseenEvents;
        }

        @Override
        public void onPrepareCommit(UnitOfWork unitOfWork, Set<AggregateRoot> aggregateRoots,
                                    List<EventProxy> events) {
            if (hasPotentialConflicts()) {
                conflictResolver.resolveConflicts(asList(aggregate.getUncommittedEvents()), unseenEvents);
            }
        }

        private boolean hasPotentialConflicts() {
            return aggregate.getUncommittedEventCount() > 0
                    && aggregate.getVersion() != null
                    && !unseenEvents.isEmpty();
        }
    }

    /**
     * Wrapper around a AggregateEventStream that captures all passing events of which the sequence number is larger than
     * the expected version number.
     */
    private static final class CapturingEventStream implements AggregateEventStream, Closeable {

        private final AggregateEventStream eventStream;
        private final List<AggregateEvent> unseenEvents;
        private final Long expectedVersion;

        private CapturingEventStream(AggregateEventStream events, List<AggregateEvent> unseenEvents,
                                     Long expectedVersion) {
            eventStream = events;
            this.unseenEvents = unseenEvents;
            this.expectedVersion = expectedVersion;
        }

        @Override
        public boolean hasNext() {
            return eventStream.hasNext();
        }

        @Override
        public AggregateEvent next() {
            AggregateEvent next = eventStream.next();
            if (expectedVersion != null && next.getSequenceNumber() > expectedVersion) {
                unseenEvents.add(next);
            }
            return next;
        }

        @Override
        public AggregateEvent peek() {
            return eventStream.peek();
        }

        @Override
        public void close() throws IOException {
            IOUtils.closeQuietlyIfCloseable(eventStream);
        }
    }

}
