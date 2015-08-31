package org.sluckframework.domain.repository;

import org.sluckframework.common.exception.Assert;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.SaveAggregateCallback;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 抽象仓储的实现
 * 
 * @author sunxy
 * @time 2015年9月1日 上午12:28:08
 * @since 1.0
 */
public abstract class AbstractRepository<T extends AggregateRoot<ID>, ID extends Identifier<?>> implements Repository<T, ID> {

    private final Class<T> aggregateType;
    private final SimpleSaveAggregateCallback saveAggregateCallback = new SimpleSaveAggregateCallback();
    private EventBus eventBus;

    /**
     * 使用指定的聚合类型初始化
     *
     * @param aggregateType The type of aggregate stored in this repository
     */
    protected AbstractRepository(Class<T> aggregateType) {
        Assert.notNull(aggregateType, "aggregateType may not be null");
        this.aggregateType = aggregateType;
    }

    /**
     * 增加聚合到仓储，在uow中注册聚合，在uow提交时 聚合事件将被提交
     */
    @Override
    public void add(T aggregate) {
        Assert.isTrue(aggregateType.isInstance(aggregate), "Unsuitable aggregate for this repository: wrong type");
        if (aggregate.getVersion() != null) {
            throw new IllegalArgumentException("Only newly created (unpersisted) aggregates may be added.");
        }
        CurrentUnitOfWork.get().registerAggregate(aggregate, eventBus, saveAggregateCallback);
    }

    /**
     * 加载聚合，在uow中注册聚合，在uow提交时 聚合事件将被提交
     */
    @Override
    public T load(ID aggregateIdentifier, Long expectedVersion) {
        T aggregate = doLoad(aggregateIdentifier, expectedVersion);
        validateOnLoad(aggregate, expectedVersion);
        return CurrentUnitOfWork.get().registerAggregate(aggregate, eventBus, saveAggregateCallback);
    }

    @Override
    public T load(ID aggregateIdentifier) {
        return load(aggregateIdentifier, null);
    }

    /**
     * 验证聚合版本，同步检测
     *
     * @param aggregate       The loaded aggregate
     * @param expectedVersion The expected version of the aggregate
     * @throws ConflictingModificationException
     *
     * @throws ConflictingAggregateVersionException
     *
     */
    protected void validateOnLoad(T aggregate, Long expectedVersion) {
        if (expectedVersion != null && aggregate.getVersion() != null &&
                !expectedVersion.equals(aggregate.getVersion())) {
            throw new ConflictingAggregateVersionException(aggregate.getIdentifier(),
                                                           expectedVersion,
                                                           aggregate.getVersion());
        }
    }

    /**
     * Returns the aggregate type 
     *
     * @return the aggregate type stored by this repository
     */
    protected Class<T> getAggregateType() {
        return aggregateType;
    }

    /**
     * Performs the actual saving of the aggregate.
     *
     * @param aggregate the aggregate to store
     */
    protected abstract void doSave(T aggregate);

    /**
     * 加载 和 初始化 聚合
     *
     * @param aggregateIdentifier the identifier of the aggregate to load
     * @param expectedVersion     The expected version of the aggregate to load
     * @return a fully initialized aggregate
     */
    protected abstract T doLoad(ID aggregateIdentifier, Long expectedVersion);

    /**
     * 从仓储中移除聚合
     * @param aggregate the aggregate to delete
     */
    protected abstract void doDelete(T aggregate);

    /**
     * Sets the event bus 
     *
     * @param eventBus the event bus to publish events to
     */
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * 在 聚合更新后 和 聚合提交未提交的事件后 执行
     * @param aggregate The aggregate instance being saved
     */
    protected void postSave(T aggregate) {
    }

    /**
     * 在 聚合更新后 和 聚合提交未提交的事件后 执行
     * @param aggregate The aggregate instance being saved
     */
    protected void postDelete(T aggregate) {
    }

    private class SimpleSaveAggregateCallback implements SaveAggregateCallback<T> {

        @Override
        public void save(final T aggregate) {
            if (aggregate.isDeleted()) {
                doDelete(aggregate);
            } else {
                doSave(aggregate);
            }
            aggregate.commitEvents();
            if (aggregate.isDeleted()) {
                postDelete(aggregate);
            } else {
                postSave(aggregate);
            }
        }
    }
}
