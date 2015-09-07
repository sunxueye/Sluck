package org.sluckframework.cqrs.commandhandling.disruptor;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.common.thread.SluckThreadFactory;
import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandBus;
import org.sluckframework.cqrs.commandhandling.CommandCallback;
import org.sluckframework.cqrs.commandhandling.CommandDispatchInterceptor;
import org.sluckframework.cqrs.commandhandling.CommandHandler;
import org.sluckframework.cqrs.commandhandling.CommandHandlerInterceptor;
import org.sluckframework.cqrs.commandhandling.CommandTargetResolver;
import org.sluckframework.cqrs.commandhandling.NoHandlerForCommandException;
import org.sluckframework.cqrs.commandhandling.interceptors.SerializationOptimizingInterceptor;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventsourcing.AggregateFactory;
import org.sluckframework.cqrs.eventsourcing.EventSourcedAggregateRoot;
import org.sluckframework.cqrs.eventsourcing.EventStreamDecorator;
import org.sluckframework.cqrs.unitofwork.TransactionManager;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.Repository;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;

/**
 * 异步的commandBus实现。效率提高，分两步操作，第一步 commandhandler处理 command，then 保存 提交 聚合事件，顺序执行
 * 限制:每次仅 允许一个聚合进行操作，仅支持 事件源的 聚合，需要事件源仓储
 * 使用 com.eaio.uuid.UUID 代替 默认的 jdk uuid,有性能上的提升
 * 
 * @author sunxy
 * @time 2015年9月7日 下午4:46:37	
 * @since 1.0
 */
public class DisruptorCommandBus implements CommandBus {

    private static final Logger logger = LoggerFactory.getLogger(DisruptorCommandBus.class);
    private static final ThreadGroup DISRUPTOR_THREAD_GROUP = new ThreadGroup("DisruptorCommandBus");

    private final ConcurrentMap<String, CommandHandler<?>> commandHandlers =
            new ConcurrentHashMap<String, CommandHandler<?>>();
    private final Disruptor<CommandHandlingEntry> disruptor;
    private final CommandHandlerInvoker[] commandHandlerInvokers;
    private final List<CommandDispatchInterceptor> dispatchInterceptors;
    private final List<CommandHandlerInterceptor> invokerInterceptors;
    private final List<CommandHandlerInterceptor> publisherInterceptors;
    private final ExecutorService executorService;
    private final boolean rescheduleOnCorruptState;
    private final long coolingDownPeriod;
    private final CommandTargetResolver commandTargetResolver;
    private final int publisherCount;
    private final int serializerCount;
    private final CommandCallback<Object> failureLoggingCallback = new FailureLoggingCommandCallback();
    private volatile boolean started = true;
    private volatile boolean disruptorShutDown = false;

    /**
     * 使用给定的 属性 初始化， 使用默认的 disruptor配置
     *
     * @param AggregateEventStore The AggregateEventStore where generated events must be stored
     * @param eventBus   The EventBus where generated events must be published
     */
    public DisruptorCommandBus(AggregateEventStore AggregateEventStore, EventBus eventBus) {
        this(AggregateEventStore, eventBus, new DisruptorConfiguration());
    }

    /**
     * 用给定的 属性 初始化
     *
     * @param AggregateEventStore    The AggregateEventStore where generated events must be stored
     * @param eventBus      The EventBus where generated events must be published
     * @param configuration The configuration for the command bus
     */
    @SuppressWarnings("rawtypes")
	public DisruptorCommandBus(AggregateEventStore AggregateEventStore, EventBus eventBus,
                               DisruptorConfiguration configuration) {
        Assert.notNull(AggregateEventStore, "AggregateEventStore may not be null");
        Assert.notNull(eventBus, "eventBus may not be null");
        Assert.notNull(configuration, "configuration may not be null");
        Executor executor = configuration.getExecutor();
        if (executor == null) {
            executorService = Executors.newCachedThreadPool(
                    new SluckThreadFactory(DISRUPTOR_THREAD_GROUP));
            executor = executorService;
        } else {
            executorService = null;
        }
        rescheduleOnCorruptState = configuration.getRescheduleCommandsOnCorruptState();
        invokerInterceptors = new ArrayList<CommandHandlerInterceptor>(configuration.getInvokerInterceptors());
        publisherInterceptors = new ArrayList<CommandHandlerInterceptor>(configuration.getPublisherInterceptors());
        dispatchInterceptors = new ArrayList<CommandDispatchInterceptor>(configuration.getDispatchInterceptors());
        TransactionManager transactionManager = configuration.getTransactionManager();
        disruptor = new Disruptor<CommandHandlingEntry>(
                new CommandHandlingEntry.Factory(configuration.getTransactionManager() != null),
                configuration.getBufferSize(),
                executor,
                configuration.getProducerType(),
                configuration.getWaitStrategy());
        commandTargetResolver = configuration.getCommandTargetResolver();

        // configure invoker Threads
        commandHandlerInvokers = initializeInvokerThreads(AggregateEventStore, configuration);
        // configure serializer Threads
        SerializerHandler[] serializerThreads = initializeSerializerThreads(configuration);
        serializerCount = serializerThreads.length;
        // configure publisher Threads
        EventPublisher[] publishers = initializePublisherThreads(AggregateEventStore, eventBus, configuration, executor,
                                                                 transactionManager);
        publisherCount = publishers.length;
        disruptor.handleExceptionsWith(new ExceptionHandler());

        EventHandlerGroup<CommandHandlingEntry> eventHandlerGroup = disruptor.handleEventsWith(commandHandlerInvokers);
        if (serializerThreads.length > 0) {
            eventHandlerGroup = eventHandlerGroup.then(serializerThreads);
            invokerInterceptors.add(new SerializationOptimizingInterceptor());
        }
        eventHandlerGroup.then(publishers);

        coolingDownPeriod = configuration.getCoolingDownPeriod();
        disruptor.start();
    }

    @SuppressWarnings("rawtypes")
	private EventPublisher[] initializePublisherThreads(AggregateEventStore AggregateEventStore, EventBus eventBus,
                                                        DisruptorConfiguration configuration, Executor executor,
                                                        TransactionManager transactionManager) {
        EventPublisher[] publishers = new EventPublisher[configuration.getPublisherThreadCount()];
        for (int t = 0; t < publishers.length; t++) {
            publishers[t] = new EventPublisher(AggregateEventStore, eventBus, executor, transactionManager,
                                               configuration.getRollbackConfiguration(), t);
        }
        return publishers;
    }

    private SerializerHandler[] initializeSerializerThreads(DisruptorConfiguration configuration) {
        if (!configuration.isPreSerializationConfigured()) {
            return new SerializerHandler[0];
        }
        Serializer serializer = configuration.getSerializer();
        SerializerHandler[] serializerThreads = new SerializerHandler[configuration.getSerializerThreadCount()];
        for (int t = 0; t < serializerThreads.length; t++) {
            serializerThreads[t] = new SerializerHandler(serializer, t, configuration.getSerializedRepresentation());
        }
        return serializerThreads;
    }

    private CommandHandlerInvoker[] initializeInvokerThreads(AggregateEventStore AggregateEventStore,
                                                             DisruptorConfiguration configuration) {
        CommandHandlerInvoker[] invokers;
        invokers = new CommandHandlerInvoker[configuration.getInvokerThreadCount()];
        for (int t = 0; t < invokers.length; t++) {
            invokers[t] = new CommandHandlerInvoker(AggregateEventStore, configuration.getCache(), t);
        }
        return invokers;
    }

    @Override
    public void dispatch(final Command<?> command) {
        dispatch(command, failureLoggingCallback);
    }

    @Override
    public <R> void dispatch(Command<?> command, CommandCallback<R> callback) {
        Assert.state(started, "CommandBus has been shut down. It is not accepting any Commands");
        Command<?> commandToDispatch = command;
        for (CommandDispatchInterceptor interceptor : dispatchInterceptors) {
            commandToDispatch = interceptor.handle(commandToDispatch);
        }
        doDispatch(commandToDispatch, callback);
    }

    /**
     * 转发命令， 当 cooling down 周期内 会 重试 命令
     *
     * @param command  The command to dispatch
     * @param callback The callback to notify when command handling is completed
     * @param <R>      The expected return type of the command
     */
    @SuppressWarnings("rawtypes")
	public <R> void doDispatch(Command command, CommandCallback<R> callback) {
        Assert.state(!disruptorShutDown, "Disruptor has been shut down. Cannot dispatch or re-dispatch commands");
        final CommandHandler<?> commandHandler = commandHandlers.get(command.getCommandName());
        if (commandHandler == null) {
            throw new NoHandlerForCommandException(format("No handler was subscribed to command [%s]",
                                                          command.getCommandName()));
        }

        RingBuffer<CommandHandlingEntry> ringBuffer = disruptor.getRingBuffer();
        int invokerSegment = 0;
        int publisherSegment = 0;
        int serializerSegment = 0;
        if ((commandHandlerInvokers.length > 1 || publisherCount > 1 || serializerCount > 1)) {
            Object aggregateIdentifier = commandTargetResolver.resolveTarget(command).getIdentifier();
            if (aggregateIdentifier != null) {
                int idHash = aggregateIdentifier.hashCode() & Integer.MAX_VALUE;
                if (commandHandlerInvokers.length > 1) {
                    invokerSegment = idHash % commandHandlerInvokers.length;
                }
                if (serializerCount > 1) {
                    serializerSegment = idHash % serializerCount;
                }
                if (publisherCount > 1) {
                    publisherSegment = idHash % publisherCount;
                }
            }
        }
        long sequence = ringBuffer.next();
        try {
            CommandHandlingEntry event = ringBuffer.get(sequence);
            event.reset(command, commandHandler, invokerSegment, publisherSegment,
                        serializerSegment, new BlacklistDetectingCallback<R>(callback,
                                                                             command,
                                                                             disruptor.getRingBuffer(),
                                                                             this,
                                                                             rescheduleOnCorruptState),
                        invokerInterceptors, publisherInterceptors
            );
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * 为 从聚合工厂中创建的实例聚合实例创建 仓储
     * 此仓储必须 被 订阅了 此commandbus的 命令处理器 使用，不能被其他实例使用，不然会有并发问题
     *
     * @param aggregateFactory The factory creating uninitialized instances of the Aggregate
     * @param <T>              The type of aggregate to create the repository for
     * @return the repository that provides access to stored aggregates
     */
    @SuppressWarnings("rawtypes")
	public <T extends EventSourcedAggregateRoot> Repository createRepository(AggregateFactory<T> aggregateFactory) {
        return createRepository(aggregateFactory, NoOpEventStreamDecorator.INSTANCE);
    }

    /**
     * 为 从聚合工厂中创建的实例聚合实 例创建 仓储 使用 只指定的 包装流
     * 此仓储必须 被 订阅了 此commandbus的 命令处理器 使用，不能被其他实例使用，不然会有并发问题厂
     *
     * @param aggregateFactory The factory creating uninitialized instances of the Aggregate
     * @param decorator        The decorator to decorate events streams with
     * @param <T>              The type of aggregate to create the repository for
     * @return the repository that provides access to stored aggregates
     */
    @SuppressWarnings("rawtypes")
	public <T extends EventSourcedAggregateRoot> Repository createRepository(AggregateFactory<T> aggregateFactory,
                                                                                EventStreamDecorator decorator) {
        for (CommandHandlerInvoker invoker : commandHandlerInvokers) {
            invoker.createRepository(aggregateFactory, decorator);
        }
        return new DisruptorRepository(aggregateFactory.getTypeIdentifier());
    }

    @Override
    public <C> void subscribe(String commandName, CommandHandler<? super C> handler) {
        commandHandlers.put(commandName, handler);
    }

    @Override
    public <C> boolean unsubscribe(String commandName, CommandHandler<? super C> handler) {
        return commandHandlers.remove(commandName, handler);
    }

    /**
     * 关闭 commandbus,不再接受新的命令，并转发已有的命令
     */
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        long lastChangeDetected = System.currentTimeMillis();
        long lastKnownCursor = disruptor.getRingBuffer().getCursor();
        while (System.currentTimeMillis() - lastChangeDetected < coolingDownPeriod && !Thread.interrupted()) {
            if (disruptor.getRingBuffer().getCursor() != lastKnownCursor) {
                lastChangeDetected = System.currentTimeMillis();
                lastKnownCursor = disruptor.getRingBuffer().getCursor();
            }
        }
        disruptorShutDown = true;
        disruptor.shutdown();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private static class FailureLoggingCommandCallback implements CommandCallback<Object> {

        @Override
        public void onSuccess(Object result) {
        }

        @Override
        public void onFailure(Throwable cause) {
            logger.info("An error occurred while handling a command.", cause);
        }
    }

	private static class DisruptorRepository<T extends EventSourcedAggregateRoot<ID>, ID extends Identifier<?>>
			implements Repository<T, ID> {

        private final String typeIdentifier;

        public DisruptorRepository(String typeIdentifier) {
            this.typeIdentifier = typeIdentifier;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T load(ID aggregateIdentifier, Long expectedVersion) {
            return (T) CommandHandlerInvoker.getRepository(typeIdentifier).load(aggregateIdentifier, expectedVersion);
        }

        @SuppressWarnings("unchecked")
        @Override
        public T load(ID aggregateIdentifier) {
            return (T) CommandHandlerInvoker.getRepository(typeIdentifier).load(aggregateIdentifier);
        }

        @SuppressWarnings("unchecked")
		@Override
        public void add(T aggregate) {
            CommandHandlerInvoker.getRepository(typeIdentifier).add(aggregate);
        }


    }

    private static class NoOpEventStreamDecorator implements EventStreamDecorator {

        public static final EventStreamDecorator INSTANCE = new NoOpEventStreamDecorator();

        @Override
        public AggregateEventStream decorateForRead(String aggregateType, Object aggregateIdentifier,
                                                 AggregateEventStream eventStream) {
            return eventStream;
        }

        @SuppressWarnings("rawtypes")
		@Override
        public AggregateEventStream decorateForAppend(String aggregateType, EventSourcedAggregateRoot aggregate,
                                                   AggregateEventStream eventStream) {
            return eventStream;
        }
    }

    private class ExceptionHandler implements com.lmax.disruptor.ExceptionHandler {

        @Override
        public void handleEventException(Throwable ex, long sequence, Object event) {
            logger.error("Exception occurred while processing a {}.",
                         ((CommandHandlingEntry) event).getCommand().getPayloadType().getSimpleName(),
                         ex);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            logger.error("Failed to start the DisruptorCommandBus.", ex);
            disruptor.shutdown();
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            logger.error("Error while shutting down the DisruptorCommandBus", ex);
        }
    }
}
