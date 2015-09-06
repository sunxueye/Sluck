package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.cache.Cache;
import org.sluckframework.cache.NoCache;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListenerAdapter;
import org.sluckframework.domain.event.eventstore.PartialStreamSupport;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.lock.LockManager;
import org.sluckframework.domain.repository.lock.PessimisticLockManager;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;


/**
 * 具有缓存功能的 ES仓储，默认只支持 悲观锁
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:46:01	
 * @since 1.0
 */
public class CachingEventSourcingRepository<T extends EventSourcedAggregateRoot<ID>, ID extends Identifier<?>>
		extends EventSourcingRepository<T, ID> {
	
    private Cache cache = NoCache.INSTANCE;
    private final boolean hasEventStorePartialReadSupport;
    private final PartialStreamSupport eventStore;

    /**
     * 使用 指定的聚合工厂 和 悲观锁 初始化
     *
     * @param aggregateFactory The factory for new aggregate instances
     * @param eventStore       The event store that holds the event streams for this repository
     */
    public CachingEventSourcingRepository(AggregateFactory<T> aggregateFactory, AggregateEventStore eventStore) {
        this(aggregateFactory, eventStore, new PessimisticLockManager());
    }

    /**
     * 使用 指定的 锁策略 和相关属性初始化
     *
     * @param aggregateFactory The factory for new aggregate instances
     * @param eventStore       The event store that holds the event streams for this repository
     * @param lockManager      The lock manager restricting concurrent access to aggregate instances
     */
    public CachingEventSourcingRepository(AggregateFactory<T> aggregateFactory, AggregateEventStore eventStore,
                                          LockManager lockManager) {
        super(aggregateFactory, eventStore, lockManager);
        this.hasEventStorePartialReadSupport = (eventStore instanceof PartialStreamSupport);
        this.eventStore = eventStore instanceof PartialStreamSupport ? (PartialStreamSupport) eventStore : null;
    }

    @Override
    public void add(T aggregate) {
        CurrentUnitOfWork.get().registerListener(new CacheClearingUnitOfWorkListener(aggregate.getIdentifier()));
        super.add(aggregate);
    }

    @Override
    protected void postSave(T aggregate) {
        super.postSave(aggregate);
        cache.put(aggregate.getIdentifier(), aggregate);
    }

    @Override
    protected void postDelete(T aggregate) {
        super.postDelete(aggregate);
        cache.put(aggregate.getIdentifier(), aggregate);
    }

    /**
     * 执行 真的 读取聚合操作， 必要前提是已经获取 聚合标识符的锁
     *
     * @param aggregateIdentifier the identifier of the aggregate to load
     * @param expectedVersion     The expected version of the aggregate
     * @return the fully initialized aggregate
     */
    @Override
    public T doLoad(ID aggregateIdentifier, Long expectedVersion) {
        T aggregate = cache.get(aggregateIdentifier);
        if (aggregate == null
                || (!hasEventStorePartialReadSupport && !hasExpectedVersion(expectedVersion, aggregate.getVersion()))) {
            // if the event store doesn't support partial stream loading, we need to load the aggregate from the event store entirely
            aggregate = super.doLoad(aggregateIdentifier, expectedVersion);
        } else if (!hasExpectedVersion(expectedVersion, aggregate.getVersion())) {
            // the event store support partial stream reading, so let's read the unseen events
            resolveConflicts(aggregate, eventStore.readEvents(getTypeIdentifier(), aggregateIdentifier,
                                                              expectedVersion + 1, aggregate.getVersion()));
        } else if (aggregate.isDeleted()) {
            throw new AggregateDeletedException(aggregateIdentifier);
        }
        CurrentUnitOfWork.get().registerListener(new CacheClearingUnitOfWorkListener(aggregateIdentifier));
        return aggregate;
    }

    private boolean hasExpectedVersion(Long expectedVersion, Long actualVersion) {
        return expectedVersion == null || (actualVersion != null && actualVersion.equals(expectedVersion));
    }

    /**
     * 设置缓存
     *
     * @param cache the cache to use
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    private class CacheClearingUnitOfWorkListener extends UnitOfWorkListenerAdapter {

        private final Object identifier;

        public CacheClearingUnitOfWorkListener(Object identifier) {
            this.identifier = identifier;
        }

        @Override
        public void onRollback(UnitOfWork unitOfWork, Throwable failureCause) {
            cache.remove(identifier);
        }
    }
}
