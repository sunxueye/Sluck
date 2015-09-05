package org.sluckframework.cqrs.eventsourcing;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.executor.DirectExecutor;
import org.sluckframework.common.util.IOUtils;
import org.sluckframework.cqrs.unitofwork.NoTransactionManager;
import org.sluckframework.cqrs.unitofwork.TransactionManager;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.eventstore.SnapshotEventStore;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.ConcurrencyException;

/**
 * 快照生成者的默认实现
 * 
 * @author sunxy
 * @time 2015年9月6日 上午12:08:46
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractSnapshotter implements Snapshotter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSnapshotter.class);

    private SnapshotEventStore eventStore;
    private Executor executor = DirectExecutor.INSTANCE;
  
	private TransactionManager transactionManager = new NoTransactionManager();

    @Override
    public void scheduleSnapshot(String typeIdentifier, Identifier<?> aggregateIdentifier) {
        executor.execute(new SilentTask(
                new TransactionalRunnableWrapper(transactionManager,
                                                 createSnapshotterTask(typeIdentifier, aggregateIdentifier)
                )));
    }

    /**
     * 创建一个 聚合快照 任务
     *
     * @param typeIdentifier      The type of the aggregate to create a snapshot for
     * @param aggregateIdentifier The identifier of the aggregate to create a snapshot for
     * @return the task containing snapshot creation logic
     */
    protected Runnable createSnapshotterTask(String typeIdentifier, Identifier<?> aggregateIdentifier) {
        return new CreateSnapshotTask(typeIdentifier, aggregateIdentifier);
    }

    /**
     * 使用给定的信息 创建聚合快照事件
     *
     * @param typeIdentifier      The aggregate's type identifier
     * @param aggregateIdentifier The identifier of the aggregate to create a snapshot for
     * @param eventStream         The event stream containing the aggregate's past events
     * @return the snapshot event for the given events, or <code>null</code> if none should be stored.
     */
    protected abstract AggregateEvent createSnapshot(String typeIdentifier, Object aggregateIdentifier,
                                                         AggregateEventStream eventStream);

    /**
     * 设置事务管理器
     *
     * @param transactionManager the transactionManager to create transactions with
     */
    public void setTxManager(TransactionManager<?> transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 返回 快照事件 存储
     *
     * @return the event store this snapshotter uses to load domain events and store snapshot events.
     */
    protected SnapshotEventStore getEventStore() {
        return eventStore;
    }

    /**
     * 设置 快照事件 存储
     *
     * @param eventStore the event store to use
     */
    public void setEventStore(SnapshotEventStore eventStore) {
        this.eventStore = eventStore;
    }

    /**
     * 返回 任务执行器
     *
     * @return the executor that executes snapshot taking tasks.
     */
    protected Executor getExecutor() {
        return executor;
    }

    /**
     * 设置 任务执行器
     *
     * @param executor the executor to execute snapshotting tasks
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    private static class TransactionalRunnableWrapper implements Runnable {

        private final Runnable command;
        private final TransactionManager transactionManager;

        public TransactionalRunnableWrapper(TransactionManager transactionManager, Runnable command) {
            this.command = command;
            this.transactionManager = transactionManager;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            Object transaction = transactionManager.startTransaction();
            try {
                command.run();
                transactionManager.commitTransaction(transaction);
            } catch (RuntimeException e) {
                transactionManager.rollbackTransaction(transaction);
                throw e;
            }
        }
    }

    private static class SilentTask implements Runnable {

        private final Runnable snapshotterTask;

        public SilentTask(Runnable snapshotterTask) {
            this.snapshotterTask = snapshotterTask;
        }

        @Override
        public void run() {
            try {
                snapshotterTask.run();
            } catch (ConcurrencyException e) {
                logger.info("An up-to-date snapshot entry already exists, ignoring this attempts.");
            } catch (RuntimeException e) {
                if (logger.isDebugEnabled()) {
                    logger.warn("An attempt to create and store a snapshot resulted in an exception:", e);
                } else {
                    logger.warn("An attempt to create and store a snapshot resulted in an exception. "
                                        + "Exception summary: {}", e.getMessage());
                }
            }
        }
    }

    private final class CreateSnapshotTask implements Runnable {

        private final String typeIdentifier;
        private final Identifier<?> aggregateIdentifier;

        private CreateSnapshotTask(String typeIdentifier, Identifier<?> aggregateIdentifier) {
            this.typeIdentifier = typeIdentifier;
            this.aggregateIdentifier = aggregateIdentifier;
        }

        @Override
        public void run() {
            AggregateEventStream eventStream = eventStore.readEvents(typeIdentifier, aggregateIdentifier);
            try {
                // a snapshot should only be stored if the snapshot replaces at least more than one event
                long firstEventSequenceNumber = eventStream.peek().getSequenceNumber();
                AggregateEvent snapshotEvent = createSnapshot(typeIdentifier, aggregateIdentifier, eventStream);
                if (snapshotEvent != null && snapshotEvent.getSequenceNumber() > firstEventSequenceNumber) {
                    eventStore.appendSnapshotEvent(typeIdentifier, snapshotEvent);
                }
            } finally {
                IOUtils.closeQuietlyIfCloseable(eventStream);
            }
        }
    }
}
