package org.sluckframework.cqrs.commandhandling;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.cqrs.commandhandling.callback.LoggingCallback;
import org.sluckframework.cqrs.unitofwork.DefaultUnitOfWorkFactory;
import org.sluckframework.cqrs.unitofwork.TransactionManager;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkFactory;
import org.sluckframework.monitoring.MonitorRegistry;

/**
 * 命令总线的简单实现，同步等待命令处理完毕后返回
 * 
 * @author sunxy
 * @time 2015年9月7日 上午9:46:40	
 * @since 1.0
 */
public class SimpleCommandBus implements CommandBus {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCommandBus.class);

    private final ConcurrentMap<String, CommandHandler<?>> subscriptions =
            new ConcurrentHashMap<String, CommandHandler<?>>();
    private final SimpleCommandBusStatistics statistics = new SimpleCommandBusStatistics();
    private volatile Iterable<? extends CommandHandlerInterceptor> handlerInterceptors = Collections.emptyList();
    private volatile Iterable<? extends CommandDispatchInterceptor> dispatchInterceptors = Collections.emptyList();
    private UnitOfWorkFactory unitOfWorkFactory = new DefaultUnitOfWorkFactory();
    private RollbackConfiguration rollbackConfiguration = new RollbackOnUncheckedExceptionConfiguration();

    /**
     * 初始化
     */
    public SimpleCommandBus() {
        MonitorRegistry.registerMonitoringBean(statistics, SimpleCommandBus.class);
    }

    @Override
    public void dispatch(Command<?> command) {
        doDispatch(intercept(command), new LoggingCallback(command));
    }

    @Override
    public <R> void dispatch(Command<?> command, final CommandCallback<R> callback) {
        doDispatch(intercept(command), callback);
    }

    /**
     * 执行所有的 dispatch 拦截器
     *
     * @param command The original command being dispatched
     * @return The command to actually dispatch
     */
    protected Command<?> intercept(Command<?> command) {
        Command<?> commandToDispatch = command;
        for (CommandDispatchInterceptor interceptor : dispatchInterceptors) {
            commandToDispatch = interceptor.handle(commandToDispatch);
        }
        return commandToDispatch;
    }

    /**
     * 执行真正的转发命令 业务逻辑
     *
     * @param command  The actual command to dispatch to the handler
     * @param callback The callback to notify of the result
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <R> void doDispatch(Command<?> command, CommandCallback<R> callback) {
        try {
            CommandHandler handler = findCommandHandlerFor(command);
            Object result = doDispatch(command, handler);
            callback.onSuccess((R) result);
        } catch (Throwable throwable) {
            callback.onFailure(throwable);
        }
    }

    @SuppressWarnings("rawtypes")
	private CommandHandler findCommandHandlerFor(Command<?> command) {
        final CommandHandler handler = subscriptions.get(command.getCommandName());
        if (handler == null) {
            throw new NoHandlerForCommandException(format("No handler was subscribed to command [%s]",
                                                          command.getCommandName()));
        }
        return handler;
    }

    @SuppressWarnings("rawtypes")
	private Object doDispatch(Command<?> command, CommandHandler commandHandler) throws Throwable {
        logger.debug("Dispatching command [{}]", command.getCommandName());
        statistics.recordReceivedCommand();
        UnitOfWork unitOfWork = unitOfWorkFactory.createUnitOfWork();
        InterceptorChain chain = new DefaultInterceptorChain(command, unitOfWork, commandHandler, handlerInterceptors);

        Object returnValue;
        try {
            returnValue = chain.proceed();
        } catch (Throwable throwable) {
            if (rollbackConfiguration.rollBackOn(throwable)) {
                unitOfWork.rollback(throwable);
            } else {
                unitOfWork.commit();
            }
            throw throwable;
        }

        unitOfWork.commit();
        return returnValue;
    }

    /**
     * 根据指定的名称订阅指定的命令， 并注册相应的命令处理器 
     *
     * @param commandName The type of command to subscribe the handler to
     * @param handler     The handler instance that handles the given type of command
     * @param <T>         The Type of command
     */
    @Override
    public <T> void subscribe(String commandName, CommandHandler<? super T> handler) {
        subscriptions.put(commandName, handler);
        statistics.reportHandlerRegistered(commandName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean unsubscribe(String commandName, CommandHandler<? super T> handler) {
        if (subscriptions.remove(commandName, handler)) {
            statistics.recordUnregisteredHandler(commandName);
            return true;
        }
        return false;
    }

    /**
     * 注册 handler 拦截器
     *
     * @param handlerInterceptors The interceptors to invoke when commands are handled
     */
    public void setHandlerInterceptors(List<? extends CommandHandlerInterceptor> handlerInterceptors) {
        this.handlerInterceptors = new ArrayList<CommandHandlerInterceptor>(handlerInterceptors);
    }

    /**
     * 注册 dispatch 拦截器
     *
     * @param dispatchInterceptors The interceptors to invoke when commands are dispatched
     */
    public void setDispatchInterceptors(List<? extends CommandDispatchInterceptor> dispatchInterceptors) {
        this.dispatchInterceptors = new ArrayList<CommandDispatchInterceptor>(dispatchInterceptors);
    }

    /**
     * 批量注册 handler处理器
     *
     * @param handlers The handlers to subscribe in the form of a Map of Class - CommandHandler entries.
     * key - name,  value - handler
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setSubscriptions(Map<?, ?> handlers) {
        for (Map.Entry<?, ?> entry : handlers.entrySet()) {
            String commandName;
            if (entry.getKey() instanceof Class) {
                commandName = ((Class) entry.getKey()).getName();
            } else {
                commandName = entry.getKey().toString();
            }
            subscribe(commandName, (CommandHandler) entry.getValue());
        }
    }

    /**
     * 设置 指定的 uow 工厂
     *
     * @param unitOfWorkFactory The UnitOfWorkFactory providing UoW instances for this Command Bus.
     */
    public void setUnitOfWorkFactory(UnitOfWorkFactory unitOfWorkFactory) {
        this.unitOfWorkFactory = unitOfWorkFactory;
    }

    /**
     * 设置指定的 事务管理器， 将会 重置 uow工厂 ，uow也使用指定的事务管理器
     *
     * @param transactionManager the transaction manager to use
     */
    @SuppressWarnings("rawtypes")
	public void setTransactionManager(TransactionManager transactionManager) {
        this.unitOfWorkFactory = new DefaultUnitOfWorkFactory(transactionManager);
    }

    /**
     * 设置指定 uow 回滚配置， 指定的异常 才回滚
     *
     * @param rollbackConfiguration The RollbackConfiguration.
     */
    public void setRollbackConfiguration(RollbackConfiguration rollbackConfiguration) {
        this.rollbackConfiguration = rollbackConfiguration;
    }
}
