package org.sluckframework.cqrs.commandhandling.disruptor;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandCallback;
import org.sluckframework.cqrs.commandhandling.RollbackConfiguration;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventsourcing.EventSourcedAggregateRoot;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.TransactionManager;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.AggregateNotFoundException;

/**
 * 事件发布处理器，用于存储和发布命令产生的事件
 * 
 * @author sunxy
 * @time 2015年9月7日 下午11:08:01
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class EventPublisher implements EventHandler<CommandHandlingEntry> {

    private static final Logger logger = LoggerFactory.getLogger(DisruptorCommandBus.class);

    private final AggregateEventStore AggregateEventStore;
    private final EventBus eventBus;
    private final Executor executor;
    private final RollbackConfiguration rollbackConfiguration;
    private final int segmentId;
    private final Set<Object> blackListedAggregates = new HashSet<Object>();
	private final Map<Command, Object> failedCreateCommands = new WeakHashMap<Command, Object>();
    private final TransactionManager transactionManager;

    /**
     * 使用指定的 参数初始化
     *
     * @param AggregateEventStore            The AggregateEventStore persisting the generated events
     * @param eventBus              The EventBus to publish events on
     * @param executor              The executor which schedules response reporting
     * @param transactionManager    The transaction manager that manages the transaction around event storage and
     *                              publication
     * @param rollbackConfiguration The configuration that indicates which exceptions should result in a UnitOfWork
     * @param segmentId             The ID of the segment this publisher should handle
     */
    public EventPublisher(AggregateEventStore AggregateEventStore, EventBus eventBus, Executor executor,
                          TransactionManager transactionManager, RollbackConfiguration rollbackConfiguration,
                          int segmentId) {
        this.AggregateEventStore = AggregateEventStore;
        this.eventBus = eventBus;
        this.executor = executor;
        this.transactionManager = transactionManager;
        this.rollbackConfiguration = rollbackConfiguration;
        this.segmentId = segmentId;
    }

    @Override
    public void onEvent(CommandHandlingEntry entry, long sequence, boolean endOfBatch) throws Exception {
        if (entry.isRecoverEntry()) {
            recoverAggregate(entry);
        } else if (entry.getPublisherId() == segmentId) {
            if (entry.getExceptionResult() instanceof AggregateNotFoundException
                    && failedCreateCommands.remove(entry.getCommand()) == null) {
                // the command failed for the first time
                reschedule(entry);
            } else {
                DisruptorUnitOfWork unitOfWork = entry.getUnitOfWork();
                CurrentUnitOfWork.set(unitOfWork);
                try {
                    EventSourcedAggregateRoot aggregate = unitOfWork.getAggregate();
                    if (aggregate != null && blackListedAggregates.contains(aggregate.getIdentifier())) {
                        rejectExecution(entry, unitOfWork, entry.getAggregateIdentifier());
                    } else {
                        processPublication(entry, unitOfWork, aggregate);
                    }
                } finally {
                    CurrentUnitOfWork.clear(unitOfWork);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void reschedule(CommandHandlingEntry entry) {
        failedCreateCommands.put(entry.getCommand(), logger);
        executor.execute(new ReportResultTask(
                entry.getCallback(), null,
                new AggregateStateCorruptedException(
                        entry.getAggregateIdentifier(), "Rescheduling command for execution. "
                        + "It was executed against a potentially recently created command")));
    }

    private void recoverAggregate(CommandHandlingEntry entry) {
        if (blackListedAggregates.remove(entry.getAggregateIdentifier())) {
            logger.info("Reset notification for {} received. The aggregate is removed from the blacklist",
                        entry.getAggregateIdentifier());
        }
    }

    @SuppressWarnings("unchecked")
    private void rejectExecution(CommandHandlingEntry entry, DisruptorUnitOfWork unitOfWork,
                                 Identifier<?> aggregateIdentifier) {
        executor.execute(new ReportResultTask(
                entry.getCallback(), null,
                new AggregateStateCorruptedException(
                        unitOfWork.getAggregate().getIdentifier(),
                        format("Aggregate %s has been blacklisted and will be ignored until "
                                       + "its state has been recovered.",
                               aggregateIdentifier))));
    }

    @SuppressWarnings("unchecked")
    private void processPublication(CommandHandlingEntry entry, DisruptorUnitOfWork unitOfWork,
                                    EventSourcedAggregateRoot aggregate) {
        invokeInterceptorChain(entry);
        Throwable exceptionResult = entry.getExceptionResult();
        try {
            if (exceptionResult != null && rollbackConfiguration.rollBackOn(exceptionResult)) {
                exceptionResult = performRollback(unitOfWork, entry.getAggregateIdentifier(), exceptionResult);
            } else {
                exceptionResult = performCommit(unitOfWork, aggregate, exceptionResult);
            }
        } finally {
            unitOfWork.onCleanup();
        }
        if (exceptionResult != null || entry.getCallback().hasDelegate()) {
            executor.execute(new ReportResultTask(entry.getCallback(), entry.getResult(), exceptionResult));
        }
    }

    private void invokeInterceptorChain(CommandHandlingEntry entry) {
        try {
            entry.setResult(entry.getPublisherInterceptorChain().proceed(entry.getCommand()));
        } catch (Throwable throwable) {
            entry.setExceptionResult(throwable);
        }
    }

    private Throwable performRollback(DisruptorUnitOfWork unitOfWork, Identifier<?> aggregateIdentifier,
                                      Throwable exceptionResult) {
        unitOfWork.onRollback(exceptionResult);
        if (aggregateIdentifier != null) {
            exceptionResult = notifyBlacklisted(unitOfWork, aggregateIdentifier, exceptionResult);
        }
        return exceptionResult;
    }

    @SuppressWarnings("unchecked")
    private Throwable performCommit(DisruptorUnitOfWork unitOfWork, EventSourcedAggregateRoot aggregate,
                                    Throwable exceptionResult) {
        unitOfWork.onPrepareCommit();
        Object transaction = null;
        try {
            if (exceptionResult != null && rollbackConfiguration.rollBackOn(exceptionResult)) {
                unitOfWork.rollback(exceptionResult);
            } else {
                if (transactionManager != null) {
                    transaction = transactionManager.startTransaction();
                }
                storeAndPublish(unitOfWork);
                if (transaction != null) {
                    unitOfWork.onPrepareTransactionCommit(transaction);
                    transactionManager.commitTransaction(transaction);
                }
                unitOfWork.onAfterCommit();
            }
        } catch (Exception e) {
            try {
                if (transaction != null) {
                    transactionManager.rollbackTransaction(transaction);
                }
            } catch (Exception te) {
                logger.info("Failed to explicitly rollback the transaction: ", te);
            }
            if (aggregate != null) {
                exceptionResult = notifyBlacklisted(unitOfWork, aggregate.getIdentifier(), e);
            } else {
                exceptionResult = e;
            }
        }
        return exceptionResult;
    }

    private void storeAndPublish(DisruptorUnitOfWork unitOfWork) {
        AggregateEventStream eventsToStore = unitOfWork.getEventsToStore();
        if (eventsToStore.hasNext()) {
            AggregateEventStore.appendEvents(unitOfWork.getAggregateType(), eventsToStore);
        }
        List<EventProxy> EventProxys = unitOfWork.getEventsToPublish();
        EventProxy[] eventsToPublish = EventProxys.toArray(new EventProxy[EventProxys.size()]);
        if (eventBus != null && eventsToPublish.length > 0) {
            eventBus.publish(eventsToPublish);
        }
    }

    private Throwable notifyBlacklisted(DisruptorUnitOfWork unitOfWork, Identifier<?> aggregateIdentifier,
                                        Throwable cause) {
        Throwable exceptionResult;
        blackListedAggregates.add(aggregateIdentifier);
        exceptionResult = new AggregateBlacklistedException(
                aggregateIdentifier,
                format("Aggregate %s state corrupted. "
                               + "Blacklisting the aggregate until a reset message has been received",
                       aggregateIdentifier), cause);
        unitOfWork.onRollback(exceptionResult);
        return exceptionResult;
    }

    private static class ReportResultTask<R> implements Runnable {

        private final CommandCallback<R> callback;
        private final R result;
        private final Throwable exceptionResult;

        public ReportResultTask(CommandCallback<R> callback, R result, Throwable exceptionResult) {
            this.callback = callback;
            this.result = result;
            this.exceptionResult = exceptionResult;
        }

        @Override
        public void run() {
            if (exceptionResult != null) {
                callback.onFailure(exceptionResult);
            } else {
                callback.onSuccess(result);
            }
        }
    }

}
