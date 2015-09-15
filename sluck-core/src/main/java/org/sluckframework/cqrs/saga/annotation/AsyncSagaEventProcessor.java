package org.sluckframework.cqrs.saga.annotation;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.exception.SluckNonTransientException;
import org.sluckframework.cqrs.eventhanding.async.RetryPolicy;
import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.AssociationValues;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.cqrs.saga.SagaRepository;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkFactory;

import java.util.*;

/**
 * disruptor 的事件处理器, 处理转发过来的包含saga的事件, 根据hash 算法路由每个saga
 *
 * Author: sunxy
 * Created: 2015-09-15 22:06
 * Since: 1.0
 */
public final class AsyncSagaEventProcessor implements EventHandler<AsyncSagaProcessingEvent>, LifecycleAware {

    private static final Logger logger = LoggerFactory.getLogger(AsyncSagaEventProcessor.class);
    private final UnitOfWorkFactory unitOfWorkFactory;
    private final SagaRepository sagaRepository;
    private final Map<String, Saga> processedSagas = new TreeMap<>();
    private final Map<String, Saga> newlyCreatedSagas = new TreeMap<>();
    private final ParameterResolverFactory parameterResolverFactory;
    private final int processorCount;
    private final int processorId;
    private final RingBuffer<AsyncSagaProcessingEvent> ringBuffer;
    private final AsyncAnnotatedSagaManager.SagaManagerStatus status;
    private UnitOfWork unitOfWork;
    private final ErrorHandler errorHandler;

    private AsyncSagaEventProcessor(SagaRepository sagaRepository, ParameterResolverFactory parameterResolverFactory,
                                    int processorCount, int processorId,
                                    UnitOfWorkFactory unitOfWorkFactory,
                                    RingBuffer<AsyncSagaProcessingEvent> ringBuffer,
                                    AsyncAnnotatedSagaManager.SagaManagerStatus status,
                                    ErrorHandler errorHandler) {
        this.sagaRepository = sagaRepository;
        this.parameterResolverFactory = parameterResolverFactory;
        this.processorCount = processorCount;
        this.processorId = processorId;
        this.unitOfWorkFactory = unitOfWorkFactory;
        this.ringBuffer = ringBuffer;
        this.status = status;
        this.errorHandler = errorHandler;
    }

    /**
     * 创建 disruptor event handlers 去执行saga
     *
     * @param sagaRepository           The repository which provides access to the Sagas
     * @param parameterResolverFactory The parameter resolver to resolve parameters of annotated methods
     * @param unitOfWorkFactory        The factory to create Unit of Work instances with
     * @param processorCount           The number of processors to create
     * @param ringBuffer               The ringBuffer on which the Processor will operate
     * @param status                   The object providing insight in the status of the SagaManager     @return an
     *                                 array containing the Disruptor Event Handlers to invoke Sagas.
     * @param errorHandler             Defines the behavior when errors occur while preparing or executing saga
     *                                 invocation
     *
     * @return the processor instances that will process the incoming events
     */
    static EventHandler<AsyncSagaProcessingEvent>[] createInstances(
            SagaRepository sagaRepository, ParameterResolverFactory parameterResolverFactory,
            UnitOfWorkFactory unitOfWorkFactory, int processorCount,
            RingBuffer<AsyncSagaProcessingEvent> ringBuffer, AsyncAnnotatedSagaManager.SagaManagerStatus status,
            ErrorHandler errorHandler) {
        AsyncSagaEventProcessor[] processors = new AsyncSagaEventProcessor[processorCount];
        for (int processorId = 0; processorId < processorCount; processorId++) {
            processors[processorId] = new AsyncSagaEventProcessor(sagaRepository,
                    parameterResolverFactory,
                    processorCount,
                    processorId,
                    unitOfWorkFactory,
                    ringBuffer,
                    status,
                    errorHandler);
        }
        return processors;
    }

    @Override
    public void onEvent(AsyncSagaProcessingEvent entry, long sequence, boolean endOfBatch) throws Exception {
        doProcessEvent(entry, sequence, endOfBatch);
    }

    private void doProcessEvent(final AsyncSagaProcessingEvent entry, long sequence, boolean endOfBatch)
            throws Exception {
        prepareSagas(entry);
        boolean sagaInvoked = invokeSagas(entry);
        AssociationValue associationValue;
        switch (entry.getCreationHandler().getCreationPolicy()) {
            case ALWAYS:
                associationValue = entry.getInitialAssociationValue();
                if (associationValue != null && ownedByCurrentProcessor(entry.getNewSaga().getSagaIdentifier())) {
                    processNewSagaInstance(entry, associationValue);
                }
                break;
            case IF_NONE_FOUND:
                associationValue = entry.getInitialAssociationValue();
                boolean shouldCreate = associationValue != null && entry.waitForSagaCreationVote(
                        sagaInvoked, processorCount, ownedByCurrentProcessor(entry.getNewSaga()
                                .getSagaIdentifier()));
                if (shouldCreate) {
                    processNewSagaInstance(entry, associationValue);
                }
        }

        if (endOfBatch) {
            int attempts = 0;
            while (!persistProcessedSagas(attempts == 0) && status.isRunning()) {
                if (attempts == 0) {
                    logger.warn("Error committing Saga state to the repository. Starting retry procedure...");
                }
                attempts++;
                if (attempts > 1 && attempts < 5) {
                    logger.info("Waiting 100ms for next attempt");
                    Thread.sleep(100);
                } else if (attempts >= 5) {
                    logger.info("Waiting 2000ms for next attempt");
                    long timeToStop = System.currentTimeMillis() + 2000;
                    while (inFuture(timeToStop) && isLastInBacklog(sequence) && status.isRunning()) {
                        Thread.sleep(100);
                    }
                }
            }
            if (attempts != 0) {
                logger.info("Succesfully committed. Moving on...");
            }
        }
    }

    private void prepareSagas(final AsyncSagaProcessingEvent entry) throws InterruptedException {
        boolean requiresRetry = false;
        int invocationCount = 0;
        while (invocationCount == 0 || requiresRetry) {
            requiresRetry = false;
            ensureActiveUnitOfWork();
            try {
                invocationCount++;
                Set<String> sagaIds = new HashSet<>();
                for (AssociationValue associationValue : entry.getAssociationValues()) {
                    sagaIds.addAll(sagaRepository.find(entry.getSagaType(), associationValue));
                }
                for (String sagaId : sagaIds) {
                    if (ownedByCurrentProcessor(sagaId) && !processedSagas.containsKey(sagaId)) {
                        ensureActiveUnitOfWork();
                        final Saga saga = sagaRepository.load(sagaId);
                        if (parameterResolverFactory != null) {
                            ((AbstractAnnotatedSaga) saga).registerParameterResolverFactory(parameterResolverFactory);
                        }
                        processedSagas.put(sagaId, saga);
                    }
                }
            } catch (Exception e) {
                RetryPolicy retryPolicy = errorHandler.onErrorPreparing(entry.getSagaType(),
                        entry.getPublishedEvent(),
                        invocationCount,
                        e);
                if (retryPolicy.requiresRollback()) {
                    rollbackUnitOfWork(e);
                }
                requiresRetry = retryPolicy.requiresRescheduleEvent();
                if (requiresRetry && retryPolicy.waitTime() > 0) {
                    Thread.sleep(retryPolicy.waitTime());
                }
            }
        }
    }

    private boolean inFuture(long timestamp) {
        return System.currentTimeMillis() < timestamp;
    }

    private boolean invokeSagas(final AsyncSagaProcessingEvent entry) throws InterruptedException {
        final Class<? extends Saga> sagaType = entry.getSagaType();
        boolean sagaInvoked = false;
        for (Saga saga : processedSagas.values()) {
            if (sagaType.isInstance(saga) && saga.isActive()
                    && containsAny(saga.getAssociationValues(), entry.getAssociationValues())) {
                boolean requiresRetry = false;
                int invocationCount = 0;
                while (invocationCount == 0 || requiresRetry) {
                    try {
                        ensureActiveUnitOfWork();
                        invocationCount++;
                        saga.handle(entry.getPublishedEvent());
                    } catch (Exception e) {
                        RetryPolicy retryPolicy = errorHandler.onErrorInvoking(saga, entry.getPublishedEvent(),
                                invocationCount, e);
                        if (retryPolicy.requiresRollback()) {
                            rollbackUnitOfWork(e);
                        }
                        requiresRetry = retryPolicy.requiresRescheduleEvent();
                        if (requiresRetry && retryPolicy.waitTime() > 0) {
                            Thread.sleep(retryPolicy.waitTime());
                        }
                    }
                }
                sagaInvoked = true;
            }
        }
        return sagaInvoked;
    }

    private boolean containsAny(AssociationValues associationValues, Collection<AssociationValue> toFind) {
        for (AssociationValue valueToFind : toFind) {
            if (associationValues.contains(valueToFind)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean persistProcessedSagas(boolean logExceptions) throws Exception {
        try {
            if (!processedSagas.isEmpty()) {
                Set<String> committedSagas = new HashSet<String>();
                ensureActiveUnitOfWork();
                for (Saga saga : processedSagas.values()) {
                    if (newlyCreatedSagas.containsKey(saga.getSagaIdentifier())) {
                        sagaRepository.add(saga);
                    } else {
                        sagaRepository.commit(saga);
                    }
                    committedSagas.add(saga.getSagaIdentifier());
                }
                unitOfWork.commit();
                processedSagas.keySet().removeAll(committedSagas);
                newlyCreatedSagas.keySet().removeAll(committedSagas);
            }
            return true;
        } catch (Exception e) {
            if (SluckNonTransientException.isCauseOf(e)) {
                throw e;
            }
            if (logExceptions) {
                logger.warn("Exception while attempting to persist Sagas", e);
            }
            rollbackUnitOfWork(e);
            return false;
        }
    }

    private boolean isLastInBacklog(long sequence) {
        return ringBuffer.getCursor() <= sequence;
    }

    private void processNewSagaInstance(AsyncSagaProcessingEvent entry, AssociationValue associationValue) {
        ensureActiveUnitOfWork();
        final AbstractAnnotatedSaga newSaga = entry.getNewSaga();
        if (parameterResolverFactory != null) {
            newSaga.registerParameterResolverFactory(parameterResolverFactory);
        }
        newSaga.associateWith(associationValue);
        newSaga.handle(entry.getPublishedEvent());
        processedSagas.put(newSaga.getSagaIdentifier(), newSaga);
        newlyCreatedSagas.put(newSaga.getSagaIdentifier(), newSaga);
    }

    private void ensureActiveUnitOfWork() {
        if (unitOfWork == null || !unitOfWork.isStarted()) {
            unitOfWork = unitOfWorkFactory.createUnitOfWork();
        }
    }

    private void rollbackUnitOfWork(Exception e) {
        if (unitOfWork != null && unitOfWork.isStarted()) {
            unitOfWork.rollback(e);
        }
    }

    private boolean ownedByCurrentProcessor(String sagaIdentifier) {
        return processedSagas.containsKey(sagaIdentifier)
                || Math.abs(sagaIdentifier.hashCode() & Integer.MAX_VALUE) % processorCount == processorId;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onShutdown() {
        try {
            if (!persistProcessedSagas(true)) {
                logger.error(
                        "The processor was shut down while some Saga instances could not be persisted. As a result,"
                                + "persisted Saga state may not properly reflect the activity of those Sagas.");
            }
        } catch (Exception e) {
            logger.error("A fatal, non-transient exception occurred while attempting to persist Saga state", e);
        }
    }
}
