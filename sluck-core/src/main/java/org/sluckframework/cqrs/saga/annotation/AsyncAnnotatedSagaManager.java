package org.sluckframework.cqrs.saga.annotation;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.annotation.ClasspathParameterResolverFactory;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.exception.SluckConfigurationException;
import org.sluckframework.cqrs.eventhanding.EventProcessingMonitor;
import org.sluckframework.cqrs.eventhanding.EventProcessingMonitorCollection;
import org.sluckframework.cqrs.eventhanding.EventProcessingMonitorSupport;
import org.sluckframework.cqrs.saga.*;
import org.sluckframework.cqrs.saga.repository.inmemory.InMemorySagaRepository;
import org.sluckframework.cqrs.unitofwork.DefaultUnitOfWorkFactory;
import org.sluckframework.cqrs.unitofwork.TransactionManager;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkFactory;
import org.sluckframework.domain.event.EventProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * 异步的 saga 处理器的实现,使用异步的处理器队列处理事件
 *
 * Author: sunxy
 * Created: 2015-09-15 22:33
 * Since: 1.0
 */
public class AsyncAnnotatedSagaManager implements SagaManager, EventProcessingMonitorSupport {

    private static final WaitStrategy DEFAULT_WAIT_STRATEGY = new BlockingWaitStrategy();
    private WaitStrategy waitStrategy = DEFAULT_WAIT_STRATEGY;
    private static final int DEFAULT_BUFFER_SIZE = 512;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private static final int DEFAULT_PROCESSOR_COUNT = 1;
    private int processorCount = DEFAULT_PROCESSOR_COUNT;
    private final Class<? extends AbstractAnnotatedSaga>[] sagaTypes;
    private final ParameterResolverFactory parameterResolverFactory;
    private final SagaManagerStatus sagaManagerStatus = new SagaManagerStatus();
    private final EventProcessingMonitorCollection processingMonitors = new EventProcessingMonitorCollection();
    private volatile Disruptor<AsyncSagaProcessingEvent> disruptor;
    private boolean shutdownExecutorOnStop = true;
    private Executor executor = Executors.newCachedThreadPool();
    private SagaRepository sagaRepository = new InMemorySagaRepository();
    private volatile SagaFactory sagaFactory = new GenericSagaFactory();
    private UnitOfWorkFactory unitOfWorkFactory = new DefaultUnitOfWorkFactory();
    private long startTimeout = 5000;
    private ErrorHandler errorHandler = new ProceedingErrorHandler();


    /**
     * 使用指定的 sagaType 初始化
     *
     * @param sagaTypes The types of Saga this saga manager will process incoming events for
     */
    public AsyncAnnotatedSagaManager(Class<? extends AbstractAnnotatedSaga>... sagaTypes) {
        this(ClasspathParameterResolverFactory.forClass(
                        sagaTypes.length == 0 ? AsyncAnnotatedSagaManager.class : sagaTypes[0]),
                sagaTypes);
    }

    /**
     * 使用指定的参数工厂和 sagaType 初始化
     *
     * @param parameterResolverFactory The parameter resolver factory to resolve parameters of annotated handlers
     * @param sagaTypes                The types of Saga this saga manager will process incoming events for
     */
    public AsyncAnnotatedSagaManager(ParameterResolverFactory parameterResolverFactory,
                                     Class<? extends AbstractAnnotatedSaga>... sagaTypes) {
        this.parameterResolverFactory = parameterResolverFactory;
        this.sagaTypes = Arrays.copyOf(sagaTypes, sagaTypes.length);
    }

    /**
     * 开始管理器
     */
    public synchronized void start() {
        if (disruptor == null) {
            sagaManagerStatus.setStatus(true);
            disruptor = new Disruptor<AsyncSagaProcessingEvent>(new AsyncSagaProcessingEvent.Factory(),
                    bufferSize,
                    new ValidatingExecutor(executor, startTimeout),
                    ProducerType.MULTI,
                    waitStrategy);
            disruptor.handleExceptionsWith(new LoggingExceptionHandler());
            disruptor.handleEventsWith(AsyncSagaEventProcessor.createInstances(sagaRepository, parameterResolverFactory,
                    unitOfWorkFactory, processorCount,
                    disruptor.getRingBuffer(),
                    sagaManagerStatus, errorHandler))
                    .then(new MonitorNotifier(processingMonitors));
            disruptor.start();
        }
    }

    /**
     * 停止 saga 管理器
     */
    public synchronized void stop() {
        sagaManagerStatus.setStatus(false);
        if (disruptor != null) {
            disruptor.shutdown();
            if (shutdownExecutorOnStop && executor instanceof ExecutorService) {
                ((ExecutorService) executor).shutdown();
            }
        }
        disruptor = null;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void handle(final EventProxy<?> event) {
        if (disruptor != null) {
            for (final Class<? extends AbstractAnnotatedSaga> sagaType : sagaTypes) {
                SagaMethodMessageHandlerInspector inspector =
                        SagaMethodMessageHandlerInspector.getInstance(sagaType, parameterResolverFactory);
                final List<SagaMethodMessageHandler> handlers = inspector.getMessageHandlers(event);
                if (!handlers.isEmpty()) {
                    AbstractAnnotatedSaga newSagaInstance = null;
                    for (SagaMethodMessageHandler handler : handlers) {
                        if (newSagaInstance == null && handler.getCreationPolicy() != SagaCreationPolicy.NONE) {
                            newSagaInstance = (AbstractAnnotatedSaga) sagaFactory.createSaga(inspector
                                    .getSagaType());
                        }
                    }
                    disruptor.publishEvent(new SagaProcessingEventTranslator(event, inspector, handlers,
                            newSagaInstance));
                }
            }
        }
    }

    @Override
    public Class<?> getTargetType() {
        return sagaTypes.length > 0 ? sagaTypes[0] : Void.TYPE;
    }

    @Override
    public void subscribeEventProcessingMonitor(EventProcessingMonitor monitor) {
        processingMonitors.subscribeEventProcessingMonitor(monitor);
    }

    @Override
    public void unsubscribeEventProcessingMonitor(EventProcessingMonitor monitor) {
        processingMonitors.unsubscribeEventProcessingMonitor(monitor);
    }

    /**
     * 配置执行器
     *
     * @param executor the executor that provides the threads for the processors
     * @see #setProcessorCount(int)
     */
    public synchronized void setExecutor(Executor executor) {
        Assert.state(disruptor == null, "Cannot set executor after SagaManager has started");
        this.shutdownExecutorOnStop = false;
        this.executor = executor;
    }

    /**
     * 配置 saga 仓储
     *
     * @param sagaRepository the saga repository to store and load Sagas from
     */
    public synchronized void setSagaRepository(SagaRepository sagaRepository) {
        Assert.state(disruptor == null, "Cannot set sagaRepository when SagaManager has started");
        this.sagaRepository = sagaRepository;
    }

    /**
     * 配置 saga factory
     *
     * @param sagaFactory the SagaFactory responsible for creating new Saga instances
     */
    public synchronized void setSagaFactory(SagaFactory sagaFactory) {
        Assert.state(disruptor == null, "Cannot set sagaFactory when SagaManager has started");
        this.sagaFactory = sagaFactory;
    }

    /**
     * 配置错误处理器
     *
     * @param errorHandler the error handler to notify when an error occurs
     */
    public synchronized void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * 配置事务管理器
     *
     * @param transactionManager the TransactionManager used to manage any transactions required by the underlying
     *                           storage mechanism.
     */
    public synchronized void setTransactionManager(TransactionManager transactionManager) {
        Assert.state(disruptor == null, "Cannot set transactionManager when SagaManager has started");
        this.unitOfWorkFactory = new DefaultUnitOfWorkFactory(transactionManager);
    }

    /**
     * 配置执行事件处理的线程数量
     *
     * @param processorCount the number of processors (threads) to process events with
     */
    public synchronized void setProcessorCount(int processorCount) {
        Assert.state(disruptor == null, "Cannot set processorCount when SagaManager has started");
        this.processorCount = processorCount;
    }

    /**
     * 设置 从线程池等待线程的时间
     *
     * @param startTimeout the number of millis to wait for the processor to have been assigned a thread. Defaults to
     *                     5000 (5 seconds).
     */
    public synchronized void setStartTimeout(long startTimeout) {
        this.startTimeout = startTimeout;
    }

    /**
     * 配置 ringbuffer size
     *
     * @param bufferSize The size of the processing buffer. Must be a power of 2.
     */
    public synchronized void setBufferSize(int bufferSize) {
        Assert.isTrue(Integer.bitCount(bufferSize) == 1, "The buffer size must be a power of 2");
        Assert.state(disruptor == null, "Cannot set bufferSize when SagaManager has started");
        this.bufferSize = bufferSize;
    }

    /**
     * 设置等待策略
     *
     * @param waitStrategy the WaitStrategy to use when event processors need to wait for incoming events
     */
    public synchronized void setWaitStrategy(WaitStrategy waitStrategy) {
        Assert.state(disruptor == null, "Cannot set waitStrategy when SagaManager has started");
        this.waitStrategy = waitStrategy;
    }


    private static final class SagaProcessingEventTranslator implements EventTranslator<AsyncSagaProcessingEvent> {

        private final EventProxy<?> event;
        private final SagaMethodMessageHandlerInspector annotationInspector;
        private final List<SagaMethodMessageHandler> handlers;
        private final AbstractAnnotatedSaga newSagaInstance;

        private SagaProcessingEventTranslator(EventProxy<?> event, SagaMethodMessageHandlerInspector annotationInspector,
                                              List<SagaMethodMessageHandler> handlers,
                                              AbstractAnnotatedSaga newSagaInstance) {
            this.event = event;
            this.annotationInspector = annotationInspector;
            this.handlers = handlers;
            this.newSagaInstance = newSagaInstance;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void translateTo(AsyncSagaProcessingEvent entry, long sequence) {
            entry.reset(event, annotationInspector.getSagaType(), handlers, newSagaInstance);
        }
    }

    private static final class LoggingExceptionHandler implements ExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(LoggingExceptionHandler.class);

        @Override
        public void handleEventException(Throwable ex, long sequence, Object event) {
            logger.warn("A fatal exception occurred while processing an Event for a Saga. "
                    + "Processing will continue with the next Event", ex);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            logger.warn("An exception occurred while starting the AsyncAnnotatedSagaManager.", ex);
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            logger.warn("An exception occurred while shutting down the AsyncAnnotatedSagaManager.", ex);
        }
    }

    /**
     * 运气时 saga管理器 状态
     */
    static class SagaManagerStatus {

        private volatile boolean isRunning;

        private void setStatus(boolean running) {
            isRunning = running;
        }

        public boolean isRunning() {
            return isRunning;
        }
    }

    private static class ValidatingExecutor implements Executor {

        private final Executor delegate;
        private final long timeoutMillis;

        public ValidatingExecutor(Executor executor, long timeoutMillis) {
            this.delegate = executor;
            this.timeoutMillis = timeoutMillis;
        }

        @Override
        public void execute(Runnable command) {
            final StartDetectingRunnable task = new StartDetectingRunnable(command);
            delegate.execute(task);
            try {
                if (!task.awaitStarted(timeoutMillis, TimeUnit.MILLISECONDS)) {
                    throw new SluckConfigurationException("It seems that the given Executor is not providing a thread "
                            + "for the AsyncSagaManager. Ensure that the "
                            + "corePoolSize is larger than the processor count.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class StartDetectingRunnable implements Runnable {

        private final Runnable delegate;
        private final CountDownLatch cdl = new CountDownLatch(1);

        public StartDetectingRunnable(Runnable command) {
            this.delegate = command;
        }

        @Override
        public void run() {
            cdl.countDown();
            delegate.run();
        }

        public boolean awaitStarted(long timeout, TimeUnit unit) throws InterruptedException {
            return cdl.await(timeout, unit);
        }
    }

    private static class MonitorNotifier implements EventHandler<AsyncSagaProcessingEvent> {

        private final EventProcessingMonitor monitor;
        private final List<EventProxy<?>> processedMessages = new ArrayList<EventProxy<?>>();

        public MonitorNotifier(EventProcessingMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void onEvent(AsyncSagaProcessingEvent event, long sequence, boolean endOfBatch) throws Exception {
            processedMessages.add(event.getPublishedEvent());
            if (endOfBatch) {
                monitor.onEventProcessingCompleted(processedMessages);
                processedMessages.clear();
            }
        }
    }
}
