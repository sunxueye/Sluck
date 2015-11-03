package org.sluckframework.cqrs.unitofwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.domain.event.EventProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 可以嵌套的 uow
 * 
 * @author sunxy
 * @time 2015年9月2日 下午2:21:40	
 * @since 1.0
 */
public abstract class NestableUnitOfWork implements UnitOfWork {

    private static final Logger logger = LoggerFactory.getLogger(NestableUnitOfWork.class);

    private boolean isStarted;
    private UnitOfWork outerUnitOfWork;
    private final List<NestableUnitOfWork> innerUnitsOfWork = new ArrayList<>();
    private boolean isCommitted = false;
    private final Map<String, Object> resources = new HashMap<>();
    private final Map<String, Object> inheritedResources = new HashMap<>();

    @Override
    public void commit() {
        logger.debug("Committing Unit Of Work");
        assertStarted();
        try {
            notifyListenersPrepareCommit();
            saveAggregates();
            isCommitted = true;
            if (outerUnitOfWork == null) {
                logger.debug("This Unit Of Work is not nested. Finalizing commit...");
                doCommit();
                stop();
                performCleanup();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("This Unit Of Work is nested. Commit will be finalized by outer Unit Of Work.");
                }
                registerScheduledEvents(outerUnitOfWork);
            }
        } catch (RuntimeException e) {
            logger.debug("An error occurred while committing this UnitOfWork. Performing rollback...", e);
            doRollback(e);
            stop();
            if (outerUnitOfWork == null) {
                performCleanup();
            }
            throw e;
        } finally {
            logger.debug("Clearing resources of this Unit Of Work.");
            clear();
        }
    }

    protected abstract void registerScheduledEvents(UnitOfWork unitOfWork);

    private void performCleanup() {
        for (NestableUnitOfWork uow : innerUnitsOfWork) {
            uow.performCleanup();
        }
        notifyListenersCleanup();
    }

    /**
     * 通知 监听 clean up 的监听器 
     */
    protected abstract void notifyListenersCleanup();

    /**
     * 通知 监听 Rollback 的监听器 
     *
     * @param cause The cause of the rollback
     */
    protected abstract void notifyListenersRollback(Throwable cause);

    @Override
    public void rollback() {
        rollback(null);
    }

    @Override
    public void rollback(Throwable cause) {
        if (cause != null && logger.isInfoEnabled()) {
            logger.debug("Rollback requested for Unit Of Work due to exception. ", cause);
        } else if (logger.isInfoEnabled()) {
            logger.debug("Rollback requested for Unit Of Work for unknown reason.");
        }

        try {
            if (isStarted()) {
                for (NestableUnitOfWork inner : innerUnitsOfWork) {
                    CurrentUnitOfWork.set(inner);
                    inner.rollback(cause);
                }
                doRollback(cause);
            }
        } finally {
            if (outerUnitOfWork == null) {
                performCleanup();
            }
            clear();
            stop();
        }
    }

    @Override
    public void start() {
        logger.debug("Starting Unit Of Work.");
        if (isStarted) {
            throw new IllegalStateException("UnitOfWork is already started");
        }

        doStart();
        if (CurrentUnitOfWork.isStarted()) {
            // we're nesting.
            this.outerUnitOfWork = CurrentUnitOfWork.get();
            this.outerUnitOfWork.attachInheritedResources(this);
            if (outerUnitOfWork instanceof NestableUnitOfWork) {
                ((NestableUnitOfWork) outerUnitOfWork).registerInnerUnitOfWork(this);
            } else {
                outerUnitOfWork.registerListener(new CommitOnOuterCommitTask());
            }
        }
        logger.debug("Registering Unit Of Work as CurrentUnitOfWork");
        CurrentUnitOfWork.set(this);
        isStarted = true;
    }

    @Override
    public void publishEvent(EventProxy<?> event, EventBus eventBus) {
        registerForPublication(event, eventBus, !isCommitted);
    }

    /**
     * 注册 将要发布到 制定 evetbus的 事件，uow 提交的时候 发布，notifyRegistrationHandlers表示 是否应该通知 handler
     *
     * @param event                      The Event to publish
     * @param eventBus                   The Event Bus to publish the Event on
     * @param notifyRegistrationHandlers Indicates whether event registration handlers should be notified of this event
     */
    protected abstract void registerForPublication(EventProxy<?> event, EventBus eventBus,
                                                   boolean notifyRegistrationHandlers);

    @Override
    public boolean isStarted() {
        return isStarted;
    }

    private void stop() {
        logger.debug("Stopping Unit Of Work");
        isStarted = false;
    }

    /**
     * 开始
     */
    protected abstract void doStart();

    /**
     * 执行真正的提交动作
     */
    protected abstract void doCommit();

    /**
     * 执行回滚操作
     *
     * @param cause the cause of the rollback
     */
    protected abstract void doRollback(Throwable cause);

    private void performInnerCommit() {
        logger.debug("Finalizing commit of inner Unit Of Work...");
        CurrentUnitOfWork.set(this);
        try {
            doCommit();
        } catch (RuntimeException t) {
            doRollback(t);
            throw t;
        } finally {
            clear();
            stop();
        }
    }

    private void assertStarted() {
        if (!isStarted) {
            throw new IllegalStateException("UnitOfWork is not started");
        }
    }

    private void clear() {
        CurrentUnitOfWork.clear(this);
    }

    /**
     * Commit all registered inner units of work. 
     */
    protected void commitInnerUnitOfWork() {
        // do not replace this for loop with an iterator based on, as it cannot handle concurrent modifications
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < innerUnitsOfWork.size(); i++) {
            NestableUnitOfWork unitOfWork = innerUnitsOfWork.get(i);
            if (unitOfWork.isStarted()) {
                unitOfWork.performInnerCommit();
            }
        }
    }

    private void registerInnerUnitOfWork(NestableUnitOfWork unitOfWork) {
        if (outerUnitOfWork instanceof NestableUnitOfWork) {
            ((NestableUnitOfWork) outerUnitOfWork).registerInnerUnitOfWork(unitOfWork);
        } else {
            innerUnitsOfWork.add(unitOfWork);
        }
    }

    /**
     * 调用聚合的 save callbacks 来保存聚合
      */
    protected abstract void saveAggregates();

    /**
     * 通知 注册的 监听 prepareCommit() 动作的 监听器
     */
    protected abstract void notifyListenersPrepareCommit();

    @Override
    public void attachResource(String name, Object resource) {
        this.resources.put(name, resource);
        this.inheritedResources.remove(name);
    }

    @Override
    public void attachResource(String name, Object resource, boolean inherited) {
        this.resources.put(name, resource);
        if (inherited) {
            this.inheritedResources.put(name, resource);
        } else {
            this.inheritedResources.remove(name);
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

    private final class CommitOnOuterCommitTask extends UnitOfWorkListenerAdapter {

        @Override
        public void afterCommit(UnitOfWork unitOfWork) {
            performInnerCommit();
        }

        @Override
        public void onRollback(UnitOfWork unitOfWork, Throwable failureCause) {
            CurrentUnitOfWork.set(NestableUnitOfWork.this);
            rollback(failureCause);
        }

        @Override
        public void onCleanup(UnitOfWork unitOfWork) {
            performCleanup();
        }
    }
}
