package org.sluckframework.cqrs.commandhandling.annotation;

import org.sluckframework.common.annotation.AbstractMessageHandler;
import org.sluckframework.common.annotation.ClasspathParameterResolverFactory;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.cqrs.commandhandling.*;
import org.sluckframework.cqrs.commandhandling.CommandHandler;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.Repository;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.sluckframework.cqrs.commandhandling.annotation.CommandMessageHandlerUtils.resolveAcceptedCommandName;

/**
 * 注解类型的聚合根识别 ,可以识别命令
 *
 * Author: sunxy
 * Created: 2015-09-09 19:25
 * Since: 1.0
 */
public class AggregateAnnotationCommandHandler<T extends AggregateRoot<ID>, ID extends Identifier<?>>
        implements CommandHandler<Object> {
    private final Repository<T, ID> repository;

    private final CommandTargetResolver commandTargetResolver;
    private final Map<String, CommandHandler<Object>> handlers;
    private final ParameterResolverFactory parameterResolverFactory;

    /**
     * 用给定的聚合类型和仓储初始化
     *
     * @param aggregateType The type of aggregate
     * @param repository    The repository providing access to aggregate instances
     */
    public AggregateAnnotationCommandHandler(Class<T> aggregateType, Repository<T, ID> repository) {
        this(aggregateType, repository, new AnnotationCommandTargetResolver());
    }

    /**
     * 用给定的聚合类型和仓和命令参数解析器储初始化
     *
     * @param aggregateType         The type of aggregate
     * @param repository            The repository providing access to aggregate instances
     * @param commandTargetResolver The target resolution strategy
     */
    public AggregateAnnotationCommandHandler(Class<T> aggregateType, Repository<T, ID> repository,
                                             CommandTargetResolver commandTargetResolver) {
        this(aggregateType, repository, commandTargetResolver,
                ClasspathParameterResolverFactory.forClass(aggregateType));
    }

    /**
     * 用给定的聚合类型和仓和命令参数解析器和参数工厂初始化
     *
     * @param aggregateType            The type of aggregate
     * @param repository               The repository providing access to aggregate instances
     * @param commandTargetResolver    The target resolution strategy
     * @param parameterResolverFactory The strategy for resolving parameter values for handler methods
     */
    public AggregateAnnotationCommandHandler(Class<T> aggregateType, Repository<T, ID> repository,
                                             CommandTargetResolver commandTargetResolver,
                                             ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
        Assert.notNull(aggregateType, "aggregateType may not be null");
        Assert.notNull(repository, "repository may not be null");
        Assert.notNull(commandTargetResolver, "commandTargetResolver may not be null");
        this.repository = repository;
        this.commandTargetResolver = commandTargetResolver;
        this.handlers = initializeHandlers(new AggregateCommandHandlerInspector<>(aggregateType,
                parameterResolverFactory));
    }

    /**
     * 从指定的 commandbus 订阅命令消息
     *
     * @param aggregateType The type of aggregate
     * @param repository    The repository providing access to aggregate instances
     * @param commandBus    The command bus to register command handlers to
     * @param <T>           The type of aggregate this handler handles commands for
     * @return the Adapter created for the command handler target. Can be used to unsubscribe.
     */
    public static <T extends AggregateRoot<ID>, ID extends Identifier<?>> AggregateAnnotationCommandHandler subscribe(
            Class<T> aggregateType, Repository<T, ID> repository, CommandBus commandBus) {
        AggregateAnnotationCommandHandler<T, ID> adapter = new AggregateAnnotationCommandHandler<>(aggregateType,
                repository);
        for (String supportedCommand : adapter.supportedCommands()) {
            commandBus.subscribe(supportedCommand, adapter);
        }

        return adapter;
    }

    /**
     * 从指定的 commandBus 订阅指定的聚合类型
     *
     * @param aggregateType         The type of aggregate
     * @param repository            The repository providing access to aggregate instances
     * @param commandBus            The command bus to register command handlers to
     * @param commandTargetResolver The target resolution strategy
     * @param <T>                   The type of aggregate this handler handles commands for
     * @return the Adapter created for the command handler target. Can be used to unsubscribe.
     */
    public static <T extends AggregateRoot<ID>, ID extends Identifier<?>> AggregateAnnotationCommandHandler subscribe(
            Class<T> aggregateType, Repository<T, ID> repository, CommandBus commandBus,
            CommandTargetResolver commandTargetResolver) {
        AggregateAnnotationCommandHandler<T, ID> adapter = new AggregateAnnotationCommandHandler<>(
                aggregateType, repository, commandTargetResolver);
        for (String supportedCommand : adapter.supportedCommands()) {
            commandBus.subscribe(supportedCommand, adapter);
        }
        return adapter;
    }

    /**
     * 订阅指定的commandBus命令,使用聚合命令处理器处理
     * Subscribe the given <code>aggregateAnnotationCommandHandler</code> to the given <code>commandBus</code>. The
     * command handler will be subscribed for each of the supported commands.
     *
     * @param aggregateAnnotationCommandHandler The fully configured AggregateAnnotationCommandHandler instance to
     *                                          subscribe
     * @param commandBus                        The command bus instance to subscribe to
     */
    public static void subscribe(AggregateAnnotationCommandHandler<?, ?> aggregateAnnotationCommandHandler,
                                 CommandBus commandBus) {
        for (String supportedCommand : aggregateAnnotationCommandHandler.supportedCommands()) {
            commandBus.subscribe(supportedCommand, aggregateAnnotationCommandHandler);
        }
    }

    private Map<String, CommandHandler<Object>> initializeHandlers(AggregateCommandHandlerInspector<T, ?> inspector) {
        Map<String, CommandHandler<Object>> handlersFound = new HashMap<>();
        for (final AbstractMessageHandler commandHandler : inspector.getHandlers()) {
            handlersFound.put(resolveAcceptedCommandName(commandHandler),
                    new AggregateCommandHandler(commandHandler));
        }

        for (final ConstructorCommandHandler<T> handler : inspector.getConstructorHandlers()) {
            handlersFound.put(resolveAcceptedCommandName(handler), new AggregateConstructorCommandHandler(handler));
        }
        return handlersFound;
    }
    
    /**
     * 返回可以处理命令的名称
     *
     * @return the set of commands supported by the annotated command handler
     */
    public Set<String> supportedCommands() {
        return handlers.keySet();
    }

    @Override
    public Object handle(Command<Object> commandMessage, UnitOfWork unitOfWork) throws Throwable {
        unitOfWork.attachResource(ParameterResolverFactory.class.getName(), parameterResolverFactory);
        return handlers.get(commandMessage.getCommandName()).handle(commandMessage, unitOfWork);
    }

    @SuppressWarnings("unchecked")
    private T loadAggregate(Command<?> command) {
        VersionedAggregateIdentifier iv = commandTargetResolver.resolveTarget(command);
        return repository.load((ID) iv.getIdentifier(), iv.getVersion());
    }

    /**
     * 当命令为创建聚合的时候,返回被命令创建的聚合的标示符
     *
     * @param command          The command being executed
     * @param createdAggregate The aggregate that has been created as a result of the command
     * @return The value to report as result of the command
     */
    protected Identifier<?> resolveReturnValue(Command<?> command, T createdAggregate) {
        return createdAggregate.getIdentifier();
    }

    private class AggregateConstructorCommandHandler implements CommandHandler<Object> {

        private final ConstructorCommandHandler<T> handler;

        public AggregateConstructorCommandHandler(ConstructorCommandHandler<T> handler) {
            this.handler = handler;
        }

        @Override
        public Object handle(Command<Object> command, UnitOfWork unitOfWork) throws Throwable {
            try {
                final T createdAggregate = handler.invoke(null, command);
                repository.add(createdAggregate);
                return resolveReturnValue(command, createdAggregate);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    private class AggregateCommandHandler implements CommandHandler<Object> {

        private final AbstractMessageHandler commandHandler;

        public AggregateCommandHandler(AbstractMessageHandler commandHandler) {
            this.commandHandler = commandHandler;
        }

        @Override
        public Object handle(Command<Object> command, UnitOfWork unitOfWork) throws Throwable {
            T aggregate = loadAggregate(command);
            try {
                return commandHandler.invoke(aggregate, command);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}


