package org.sluckframework.cqrs.unitofwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.aggregate.EventRegistrationCallback;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.aggregate.AggregateEvent;

/**
 * 默认的 uow 实现，只有在uow 提交的 时候 聚合 才真正的被保存， 聚合事件真正被发布
 * 
 * @author sunxy
 * @time 2015年9月2日 下午2:50:29	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class DefaultUnitOfWork extends NestableUnitOfWork {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUnitOfWork.class);

    
	private final Map<AggregateRoot, AggregateAndSaveCallback> registeredAggregates =
            new LinkedHashMap<AggregateRoot, AggregateAndSaveCallback>();
    private final Map<EventBus, List<EventProxy<?>>> eventsToPublish = new HashMap<EventBus, List<EventProxy<?>>>();
    private final UnitOfWorkListenerCollection listeners = new UnitOfWorkListenerCollection();
    private Status dispatcherStatus = Status.READY;
    private final TransactionManager transactionManager;
    private Object backingTransaction; //不能为空，如果为空，事务将不能回滚，为空则认为uow不存在事务

    /**
     * 初始化 - 不使用 事务
     */
    public DefaultUnitOfWork() {
        this(null);
    }

    /**
     * 使用 给定的 事务管理器 初始化
     *
     * @param transactionManager The transaction manager to manage the transaction around this Unit of Work
     */
    public DefaultUnitOfWork(TransactionManager<?> transactionManager) {
        this.transactionManager = transactionManager;
    }

    private static enum Status {

        READY, DISPATCHING
    }

    /**
     * 开始一个 武事务 Uow 并返回， uow 是ThreadLocal级别的 线程之间不共享
     *
     * @return the started UnitOfWork instance
     */
    public static UnitOfWork startAndGet() {
        DefaultUnitOfWork uow = new DefaultUnitOfWork();
        uow.start();
        return uow;
    }

    /**
     * 使用给定的 事务管理器 开始一个 uow，并返回 。uow 是ThreadLocal级别的 线程之间不共享
     *
     * @param transactionManager The transaction manager to provide the backing transaction. May be <code>null</code>
     *                           when not using transactions.
     * @return the started UnitOfWork instance
     */
    public static UnitOfWork startAndGet(TransactionManager<?> transactionManager) {
        DefaultUnitOfWork uow = new DefaultUnitOfWork(transactionManager);
        uow.start();
        return uow;
    }

    @Override
    protected void doStart() {
        if (isTransactional()) {
            this.backingTransaction = transactionManager.startTransaction();
        }
    }

    @Override
    public boolean isTransactional() {
        return transactionManager != null;
    }

    /** 
     * backingTransaction 如果为空的话，将不会进行事务回滚，认为没有事务
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doRollback(Throwable cause) {
        registeredAggregates.clear();
        eventsToPublish.clear();
        try {
            if (backingTransaction != null) {
                transactionManager.rollbackTransaction(backingTransaction);
            }
        } finally {
            notifyListenersRollback(cause);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doCommit() {
        do {
            publishEvents();
            commitInnerUnitOfWork();
        } while (!this.eventsToPublish.isEmpty());
        if (isTransactional()) {
            notifyListenersPrepareTransactionCommit(backingTransaction);
            transactionManager.commitTransaction(backingTransaction);
        }
        notifyListenersAfterCommit();
    }

    /** 
     * 为其他的工作单元注册本单元已注册的事件,同时把本工作单元注册的事件清除
     */
    @Override
    protected void registerScheduledEvents(UnitOfWork unitOfWork) {
        for (Map.Entry<EventBus, List<EventProxy<?>>> entry : eventsToPublish.entrySet()) {
            for (EventProxy<?> eventMessage : entry.getValue()) {
                unitOfWork.publishEvent(eventMessage, entry.getKey());
            }
        }
        eventsToPublish.clear();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T extends AggregateRoot> T registerAggregate(final T aggregate, final EventBus eventBus,
                                                         SaveAggregateCallback<T> saveAggregateCallback) {
        T similarAggregate = (T) findSimilarAggregate(aggregate.getClass(), aggregate.getIdentifier());
        if (similarAggregate != null) {
            if (logger.isInfoEnabled()) {
                logger.info("Ignoring aggregate registration. An aggregate of same type and identifier was already "
                                    + "registered in this Unit Of Work: type [{}], identifier [{}]",
                            aggregate.getClass().getSimpleName(),
                            aggregate.getIdentifier());
            }
            return similarAggregate;
        }
        EventRegistrationCallback eventRegistrationCallback = new UoWEventRegistrationCallback(eventBus);

        registeredAggregates.put(aggregate, new AggregateAndSaveCallback<T>(aggregate, saveAggregateCallback));

        // 为聚合注册  事件注册回调函数  即 聚合注册未提交的事件的时候， 会调用此回调函数 将事件注册到  callback中的 eventbus
        aggregate.addEventRegistrationCallback(eventRegistrationCallback);
        return aggregate;
    }

    /**
     * @param event 执行注册的Listner执行 当新的事件被注册用于uow提交发布的时候发布 触发 监听的这个事件
     * @return
     */
    private <T> EventProxy<T> invokeEventRegistrationListeners(EventProxy<T> event) {
        return listeners.onEventRegistered(this, event);
    }

    @SuppressWarnings({"unchecked"})
    private <T extends AggregateRoot> T findSimilarAggregate(Class<T> aggregateType, Object identifier) {
        for (AggregateRoot aggregate : registeredAggregates.keySet()) {
            if (aggregateType.isInstance(aggregate) && identifier.equals(aggregate.getIdentifier())) {
                return (T) aggregate;
            }
        }
        return null;
    }

    @Override
    public void registerListener(UnitOfWorkListener listener) {
        listeners.add(listener);
    }

    private List<EventProxy<?>> eventsToPublishOn(EventBus eventBus) {
        if (!eventsToPublish.containsKey(eventBus)) {
            eventsToPublish.put(eventBus, new ArrayList<EventProxy<?>>());
        }
        return eventsToPublish.get(eventBus);
    }

    @Override
    public void registerForPublication(EventProxy<?> event, EventBus eventBus, boolean notifyRegistrationHandlers) {
        if (logger.isDebugEnabled()) {
            logger.debug("Staging event for publishing: [{}] on [{}]",
                         event.getPayloadType().getName(),
                         eventBus.getClass().getName());
        }
        if (notifyRegistrationHandlers) {
            event = invokeEventRegistrationListeners(event);
        }
        eventsToPublishOn(eventBus).add(event);
    }

    @Override
    protected void notifyListenersRollback(Throwable cause) {
        listeners.onRollback(this, cause);
    }

    /**
     * 通知  监听 PrepareTransactionCommit 的监听器 
     *
     * @param transaction The object representing the transaction to about to be committed
     */
    protected void notifyListenersPrepareTransactionCommit(Object transaction) {
        listeners.onPrepareTransactionCommit(this, transaction);
    }

    /**
     * 通知  监听 AfterCommit 的监听器 
     */
    protected void notifyListenersAfterCommit() {
        listeners.afterCommit(this);
    }

    /**
     * 发布事件  到 对应的  event bus
     */
    protected void publishEvents() {
        logger.debug("Publishing events to the event bus");
        if (dispatcherStatus == Status.DISPATCHING) {
            // this prevents events from overtaking each other
            logger.debug("UnitOfWork is already in the dispatch process. "
                                 + "That process will publish events instead. Aborting...");
            return;
        }
        dispatcherStatus = Status.DISPATCHING;
        while (!eventsToPublish.isEmpty()) {
            Iterator<Map.Entry<EventBus, List<EventProxy<?>>>> iterator = eventsToPublish.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EventBus, List<EventProxy<?>>> entry = iterator.next();
                List<EventProxy<?>> messageList = entry.getValue();
                EventProxy<?>[] messages = messageList.toArray(new EventProxy<?>[messageList.size()]);
                if (logger.isDebugEnabled()) {
                    for (EventProxy message : messages) {
                        logger.debug("Publishing event [{}] to event bus [{}]",
                                     message.getPayloadType().getName(),
                                     entry.getKey());
                    }
                }
                // remove this entry before publication in case a new event is registered with the UoW while publishing
                iterator.remove();
                entry.getKey().publish(messages);
            }
        }

        logger.debug("All events successfully published.");
        dispatcherStatus = Status.READY;
    }

    @Override
    protected void saveAggregates() {
        logger.debug("Persisting changes to aggregates");
        for (AggregateAndSaveCallback entry : registeredAggregates.values()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Persisting changes to [{}], identifier: [{}]",
                             entry.aggregateRoot.getClass().getName(),
                             entry.aggregateRoot.getIdentifier());
            }
            entry.saveAggregate();
        }
        logger.debug("Aggregates successfully persisted");
        registeredAggregates.clear();
    }

    @Override
    protected void notifyListenersPrepareCommit() {
        listeners.onPrepareCommit(this, registeredAggregates.keySet(), eventsToPublish());
    }

    @Override
    protected void notifyListenersCleanup() {
        listeners.onCleanup(this);
    }

    private List<EventProxy> eventsToPublish() {
        List<EventProxy> events = new ArrayList<EventProxy>();
        for (Map.Entry<EventBus, List<EventProxy<?>>> entry : eventsToPublish.entrySet()) {
            events.addAll(entry.getValue());
        }
        return Collections.unmodifiableList(events);
    }

    private static class AggregateAndSaveCallback<T extends AggregateRoot> {

        private final T aggregateRoot;
        private final SaveAggregateCallback<T> callback;

        public AggregateAndSaveCallback(T aggregateRoot, SaveAggregateCallback<T> callback) {
            this.aggregateRoot = aggregateRoot;
            this.callback = callback;
        }

        public void saveAggregate() {
            callback.save(aggregateRoot);
        }
    }

    private class UoWEventRegistrationCallback implements EventRegistrationCallback {

        private final EventBus eventBus;

        public UoWEventRegistrationCallback(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        @SuppressWarnings("unchecked")
		@Override
        public AggregateEvent onRegisteredEvent(AggregateEvent event) {
            event = (AggregateEvent) invokeEventRegistrationListeners(event);
            eventsToPublishOn(eventBus).add(event);
            return event;
        }
    }
}
