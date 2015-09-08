package org.sluckframework.cqrs.commandhandling.disruptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventsourcing.EventSourcedAggregateRoot;
import org.sluckframework.cqrs.eventsourcing.EventStreamDecorator;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.SaveAggregateCallback;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListener;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListenerCollection;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.aggregate.EventRegistrationCallback;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.aggregate.SimpleAggregateEventStream;

/**
 * disruptor的 uow,因为使用 actor 模型， 所以  不需要加锁 使用 仓储的时候
 * 
 * @author sunxy
 * @time 2015年9月7日 下午4:10:59	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class DisruptorUnitOfWork implements UnitOfWork, EventRegistrationCallback{

    private static final AggregateEventStream EMPTY_DOMAIN_EVENT_STREAM = new SimpleAggregateEventStream();
    private AggregateEventStream eventsToStore = EMPTY_DOMAIN_EVENT_STREAM;
	private final List<EventProxy> eventsToPublish = new ArrayList<EventProxy>();
    private final UnitOfWorkListenerCollection listeners = new UnitOfWorkListenerCollection();
    private final boolean transactional;
    private final Map<String, Object> resources = new HashMap<String, Object>();
    private final Map<String, Object> inheritedResources = new HashMap<String, Object>();
    private boolean committed;
    private Throwable rollbackReason;
    private EventSourcedAggregateRoot aggregate;
    private String aggregateType;
    private EventStreamDecorator eventStreamDecorator;

    /**
     * 使用是否 支持 事务 进行初始化
     *
     * @param transactional Whether this Unit of Work is bound to a transaction
     */
    public DisruptorUnitOfWork(boolean transactional) {
        this.transactional = transactional;
    }

    @Override
    public void commit() {
        committed = true;
        if (aggregate != null) {
            eventsToStore = aggregate.getUncommittedEvents();
            aggregate.commitEvents();
        }
        CurrentUnitOfWork.clear(this);
    }

    /**
     * 执行 onPrepareCommit操作， 触发 监听器
     */
    public void onPrepareCommit() {
        listeners.onPrepareCommit(this,
                                  aggregate != null
                                          ? Collections.<AggregateRoot>singleton(aggregate)
                                          : Collections.<AggregateRoot>emptySet(),
                                  eventsToPublish);
    }

    /**
     * 执行 onPrepareTransactionCommit操作， 触发监听器 
     *
     * @param transaction The object representing the transaction to about to be committed
     */
    public void onPrepareTransactionCommit(Object transaction) {
        listeners.onPrepareTransactionCommit(this, transaction);
    }

    /**
     * 执行 onAfterCommit 操作 触发 监听器
     */
    public void onAfterCommit() {
        listeners.afterCommit(this);
    }

    /**
     * 执行 onCleanUp操作， 并触发 相应的监听器
     */
    public void onCleanup() {
        listeners.onCleanup(this);

        // clear the lists of events to make them garbage-collectible
        eventsToStore = EMPTY_DOMAIN_EVENT_STREAM;
        eventsToPublish.clear();
        eventStreamDecorator = null;
        this.resources.clear();
        this.inheritedResources.clear();
    }

    /**
     * 执行 onRollback 操作， 并触发监听器
     *
     * @param cause The cause of the rollback
     */
    public void onRollback(Throwable cause) {
        listeners.onRollback(this, cause);
    }

    @Override
    public void rollback() {
        rollback(null);
    }

    @Override
    public void rollback(Throwable cause) {
        rollbackReason = cause;
        if (aggregate != null) {
            aggregate.commitEvents();
        }
        CurrentUnitOfWork.clear(this);
    }

    @Override
    public void start() {
        CurrentUnitOfWork.set(this);
    }

    @Override
    public boolean isStarted() {
        return !committed && rollbackReason == null;
    }

    @Override
    public boolean isTransactional() {
        return transactional;
    }

    @Override
    public void registerListener(UnitOfWorkListener listener) {
        listeners.add(listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AggregateRoot<?>> T registerAggregate(T aggregateRoot, EventBus eventBus,
                                                         SaveAggregateCallback<T> saveAggregateCallback) {
        if (aggregateType == null) {
            throw new IllegalStateException(
                    "Cannot register an aggregate if the aggregate type of this Unit of Work hasn't been set.");
        }
        if (aggregate != null && aggregateRoot != aggregate) { // NOSONAR - Intentional equality check
            throw new IllegalArgumentException(
                    "Cannot register more than one aggregate in this Unit Of Work. Either ensure each command "
                            + "executes against at most one aggregate, or use another Command Bus implementation."
            );
        }
        aggregate = (EventSourcedAggregateRoot) aggregateRoot;

        // listen for new events registered in the aggregate
        aggregate.addEventRegistrationCallback(this);

        return (T) aggregate;
    }

    @Override
    public void attachResource(String name, Object resource) {
        this.resources.put(name, resource);
        this.inheritedResources.remove(name);
    }

    @Override
    public void attachResource(String name, Object resource, boolean inherited) {
        this.resources.put(name, resource);
        if (inherited) {
            inheritedResources.put(name, resource);
        } else {
            inheritedResources.remove(name);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getResource(String name) {
        return (T) resources.get(name);
    }

    @Override
    public void attachInheritedResources(UnitOfWork inheritingUnitOfWork) {
        for (Map.Entry<String, Object> entry : inheritedResources.entrySet()) {
            inheritingUnitOfWork.attachResource(entry.getKey(), entry.getValue(), true);
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public void publishEvent(EventProxy event, EventBus eventBus) {
        eventsToPublish.add(listeners.onEventRegistered(this, event));
    }

    /**
     * 返回 被 装饰后 的 聚合事件流
     *
     * @return the events that need to be stored as part of this Unit of Work
     */
    public AggregateEventStream getEventsToStore() {
        if (eventStreamDecorator == null) {
            return eventsToStore;
        }
        return eventStreamDecorator.decorateForAppend(aggregateType, aggregate, eventsToStore);
    }

    /**
     * 返回 uow 需要发布的 事件
     *
     * @return the events that need to be published as part of this Unit of Work
     */
    public List<EventProxy> getEventsToPublish() {
        return eventsToPublish;
    }

    /**
     * 返回 Uow 操作的 聚合 ， disruptor 一个 Uow 只能操作一个 聚合
     *
     * @return the identifier of the aggregate modified in this UnitOfWork
     */
    public EventSourcedAggregateRoot getAggregate() {
        return aggregate;
    }

    @SuppressWarnings("unchecked")
	@Override
    public AggregateEvent onRegisteredEvent(AggregateEvent event) {
        AggregateEvent message = (AggregateEvent) listeners.onEventRegistered(this, event);
        eventsToPublish.add(message);
        return message;
    }

    /**
     * 获取聚合的类型 全限定名称
     *
     * @return the type identifier of the aggregate handled in this unit of work
     */
    public String getAggregateType() {
        return aggregateType;
    }

    /**
     * 设置 聚合的 类型 权限定 名称
     *
     * @param aggregateType the type identifier of the aggregate handled in this unit of work
     */
    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    /**
     * 设置 事件流装饰器
     *
     * @param eventStreamDecorator The EventStreamDecorator to use for the event streams part of this unit of work
     */
    public void setEventStreamDecorator(EventStreamDecorator eventStreamDecorator) {
        this.eventStreamDecorator = eventStreamDecorator;
    }

}
