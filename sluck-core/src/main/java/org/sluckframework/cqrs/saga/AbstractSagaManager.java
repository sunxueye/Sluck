package org.sluckframework.cqrs.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.lock.IdentifierBasedLock;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListenerAdapter;
import org.sluckframework.domain.event.EventProxy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 *抽象的saga管理器基类,管理saga的生命周期,异步的处理事件
 *
 * Author: sunxy
 * Created: 2015-09-13 15:09
 * Since: 1.0
 */
public abstract class AbstractSagaManager implements SagaManager {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSagaManager.class);

    private final SagaRepository sagaRepository;
    private final SagaFactory sagaFactory;
    private final Class<? extends Saga>[] sagaTypes;
    private final IdentifierBasedLock lock = new IdentifierBasedLock();
    private final Map<String, Saga> sagasInCreation = new ConcurrentHashMap<>();
    private volatile boolean suppressExceptions = true;
    private volatile boolean synchronizeSagaAccess = true;

    /**
     * 使用指定的 仓储和 工厂 和类型
     *
     * @param sagaRepository The repository providing the saga instances.
     * @param sagaFactory    The factory providing new saga instances
     * @param sagaTypes      The types of Saga supported by this Saga Manager
     */
    public AbstractSagaManager(SagaRepository sagaRepository, SagaFactory sagaFactory,
                               Class<? extends Saga>... sagaTypes) {
        Assert.notNull(sagaRepository, "sagaRepository may not be null");
        Assert.notNull(sagaFactory, "sagaFactory may not be null");
        this.sagaRepository = sagaRepository;
        this.sagaFactory = sagaFactory;
        this.sagaTypes = sagaTypes;
    }

    @Override
    public void handle(final EventProxy<?> event) {
        for (Class<? extends Saga> sagaType : sagaTypes) {
            Collection<AssociationValue> associationValues = extractAssociationValues(sagaType, event);
            if (associationValues != null && !associationValues.isEmpty()) {
                boolean sagaOfTypeInvoked = invokeExistingSagas(event, sagaType, associationValues);
                SagaInitializationPolicy initializationPolicy = getSagaCreationPolicy(sagaType, event);
                if (initializationPolicy.getCreationPolicy() == SagaCreationPolicy.ALWAYS
                        || (!sagaOfTypeInvoked
                        && initializationPolicy.getCreationPolicy() == SagaCreationPolicy.IF_NONE_FOUND)) {
                    startNewSaga(event, sagaType, initializationPolicy.getInitialAssociationValue());
                }
            }
        }
    }

    private boolean invokeExistingSagas(EventProxy<?> event, Class<? extends Saga> sagaType,
                                        Collection<AssociationValue> associationValues) {
        Set<String> sagas = new TreeSet<>();
        for (AssociationValue associationValue : associationValues) {
            sagas.addAll(sagaRepository.find(sagaType, associationValue));
        }
        for (Saga sagaInCreation : sagasInCreation.values()) {
            if (sagaType.isInstance(sagaInCreation)
                    && containsAny(sagaInCreation.getAssociationValues(), associationValues)) {
                sagas.add(sagaInCreation.getSagaIdentifier());
            }
        }
        boolean sagaOfTypeInvoked = false;
        for (final String sagaId : sagas) {
            if (synchronizeSagaAccess) {
                lock.obtainLock(sagaId);
                Saga invokedSaga = null;
                try {
                    invokedSaga = loadAndInvoke(event, sagaId, associationValues);
                    if (invokedSaga != null) {
                        sagaOfTypeInvoked = true;
                    }
                } finally {
                    doReleaseLock(sagaId, invokedSaga);
                }
            } else {
                loadAndInvoke(event, sagaId, associationValues);
            }
        }
        return sagaOfTypeInvoked;
    }

    private boolean containsAny(AssociationValues associationValues, Collection<AssociationValue> toFind) {
        for (AssociationValue valueToFind : toFind) {
            if (associationValues.contains(valueToFind)) {
                return true;
            }
        }
        return false;
    }

    private void startNewSaga(EventProxy<?> event, Class<? extends Saga> sagaType, AssociationValue associationValue) {
        Saga newSaga = sagaFactory.createSaga(sagaType);
        newSaga.getAssociationValues().add(associationValue);
        preProcessSaga(newSaga);
        sagasInCreation.put(newSaga.getSagaIdentifier(), newSaga);
        try {
            if (synchronizeSagaAccess) {
                lock.obtainLock(newSaga.getSagaIdentifier());
                try {
                    doInvokeSaga(event, newSaga);
                } finally {
                    try {
                        sagaRepository.add(newSaga);
                    } finally {
                        doReleaseLock(newSaga.getSagaIdentifier(), newSaga);
                    }
                }
            } else {
                try {
                    doInvokeSaga(event, newSaga);
                } finally {
                    sagaRepository.add(newSaga);
                }
            }
        } finally {
            removeEntry(newSaga.getSagaIdentifier(), sagasInCreation);
        }
    }

    private void doReleaseLock(final String sagaId, final Saga sagaInstance) {
        if (sagaInstance == null || !CurrentUnitOfWork.isStarted()) {
            lock.releaseLock(sagaId);
        } else if (CurrentUnitOfWork.isStarted()) {
            CurrentUnitOfWork.get().registerListener(new UnitOfWorkListenerAdapter() {
                @Override
                public void onCleanup(UnitOfWork unitOfWork) {
                    // a reference to the saga is maintained to prevent it from GC until after the UoW commit
                    lock.releaseLock(sagaInstance.getSagaIdentifier());
                }
            });
        }
    }

    private void removeEntry(final String sagaIdentifier, final Map<String, ?> sagaMap) {
        if (!CurrentUnitOfWork.isStarted()) {
            sagaMap.remove(sagaIdentifier);
        } else {
            CurrentUnitOfWork.get().registerListener(new UnitOfWorkListenerAdapter() {
                @Override
                public void afterCommit(UnitOfWork unitOfWork) {
                    sagaMap.remove(sagaIdentifier);
                }
            });
        }
    }

    /**
     * 返回saga的创建策略
     *
     * @param sagaType The type of Saga to get the creation policy for
     * @param event    The Event that is being dispatched to Saga instances
     * @return the initialization policy for the Saga
     */
    protected abstract SagaInitializationPolicy getSagaCreationPolicy(Class<? extends Saga> sagaType,
                                                                      EventProxy<?> event);

    /**
     * 从指定的 事件中提取和 指定类型相关的saga的关联值
     *
     * @param sagaType The type of Saga about to handle the Event
     * @param event    The event containing the association information
     * @return the AssociationValues indicating which Sagas should handle given event
     */
    protected abstract Set<AssociationValue> extractAssociationValues(Class<? extends Saga> sagaType,
                                                                      EventProxy<?> event);

    private Saga loadAndInvoke(EventProxy<?> event, String sagaId, Collection<AssociationValue> associations) {
        Saga saga = sagasInCreation.get(sagaId);
        if (saga == null) {
            saga = sagaRepository.load(sagaId);
        }

        if (saga == null || !saga.isActive() || !containsAny(saga.getAssociationValues(), associations)) {
            return null;
        }
        preProcessSaga(saga);
        try {
            doInvokeSaga(event, saga);
        } finally {
            commit(saga);
        }
        return saga;
    }

    protected void preProcessSaga(Saga saga) {
    }

    private void doInvokeSaga(EventProxy<?> event, Saga saga) {
        try {
            saga.handle(event);
        } catch (RuntimeException e) {
            if (suppressExceptions) {
                logger.error(format("An exception occurred while a Saga [%s] was handling an Event [%s]:",
                                saga.getClass().getSimpleName(),
                                event.getPayloadType().getSimpleName()),
                        e);
            } else {
                throw e;
            }
        }
    }

    /**
     * 仓储提交 saga
     *
     * @param saga the Saga to commit.
     */
    protected void commit(Saga saga) {
        sagaRepository.commit(saga);
    }


    /**
     * 是否 打印异常
     *
     * @param suppressExceptions whether or not to suppress exceptions from Sagas.
     */
    public void setSuppressExceptions(boolean suppressExceptions) {
        this.suppressExceptions = suppressExceptions;
    }

    /**
     * 是否是同步操作
     *
     * @param synchronizeSagaAccess whether or not to synchronize access to Saga's event handlers.
     */
    public void setSynchronizeSagaAccess(boolean synchronizeSagaAccess) {
        this.synchronizeSagaAccess = synchronizeSagaAccess;
    }


    /**
     * 获取被管理的的saga的类型
     *
     * @return the set of Saga types managed by this instance.
     */
    @SuppressWarnings("unchecked")
    public Set<Class<? extends Saga>> getManagedSagaTypes() {
        return new HashSet<Class<? extends Saga>>(Arrays.asList(sagaTypes));
    }
}
