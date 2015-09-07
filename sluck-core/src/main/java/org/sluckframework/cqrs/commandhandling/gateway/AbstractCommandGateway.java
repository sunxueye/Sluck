package org.sluckframework.cqrs.commandhandling.gateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sluckframework.common.exception.Assert;
import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandBus;
import org.sluckframework.cqrs.commandhandling.CommandCallback;
import org.sluckframework.cqrs.commandhandling.CommandDispatchInterceptor;
import org.sluckframework.cqrs.commandhandling.callback.LoggingCallback;
import static org.sluckframework.cqrs.commandhandling.GenericCommand.asCommand;

/**
 * 抽象网关的基类，处理 dispatch 拦截器 和 重试机制， 实际的 发送逻辑 交于子类实现
 * 
 * @author sunxy
 * @time 2015年9月7日 下午2:19:06	
 * @since 1.0
 */
public abstract class AbstractCommandGateway implements CommandGateway{

    private final CommandBus commandBus;
    private final RetryScheduler retryScheduler;
    private final List<CommandDispatchInterceptor> dispatchInterceptors;

    /**
     * 使用指定的 cb, 重试机制， 和 dispatch 拦截器 初始化
     *
     * @param commandBus                  The command bus on which to dispatch events
     * @param retryScheduler              The scheduler capable of performing retries of failed commands. May be
     *                                    <code>null</code> when to prevent retries.
     * @param commandDispatchInterceptors The interceptors to invoke when dispatching a command
     */
    protected AbstractCommandGateway(CommandBus commandBus, RetryScheduler retryScheduler,
                                     List<CommandDispatchInterceptor> commandDispatchInterceptors) {
        Assert.notNull(commandBus, "commandBus may not be null");
        this.commandBus = commandBus;
        if (commandDispatchInterceptors != null && !commandDispatchInterceptors.isEmpty()) {
            this.dispatchInterceptors = new ArrayList<CommandDispatchInterceptor>(commandDispatchInterceptors);
        } else {
            this.dispatchInterceptors = Collections.emptyList();
        }
        this.retryScheduler = retryScheduler;
    }

    /**
     * 发送命令，当命令执行后 执行指定的 callback
     *
     * @param command  The command to dispatch
     * @param callback The callback to notify with the processing result
     * @param <R>      The type of response expected from the command
     */
    @SuppressWarnings("rawtypes")
	@Override
    public <R> void send(Object command, CommandCallback<R> callback) {
        Command Command = processInterceptors(createCommand(command));
        CommandCallback<R> commandCallback = callback;
        if (retryScheduler != null) {
            commandCallback = new RetryingCallback<R>(callback, Command, retryScheduler, commandBus);
        }
        commandBus.dispatch(Command, commandCallback);
    }

    /**
     * 发送命令，不使用 callback
     *
     * @param command The command to dispatch
     */
    protected void sendAndForget(Object command) {
        if (retryScheduler == null) {
            commandBus.dispatch(processInterceptors(createCommand(command)));
        } else {
            Command<?> Command = createCommand(command);
            send(Command, new LoggingCallback(Command));
        }
    }

    @SuppressWarnings("rawtypes")
	private Command createCommand(Object command) {
        Command<?> message = asCommand(command);
        return message;
    }

    /**
     * 执行所有的 dispatch 拦截器 并返回 应该被 dispatch 的命令
     *
     * @param Command The incoming command message
     * @return The command message to dispatch
     */
    @SuppressWarnings("rawtypes")
	protected Command processInterceptors(Command Command) {
        Command message = Command;
        for (CommandDispatchInterceptor dispatchInterceptor : dispatchInterceptors) {
            message = dispatchInterceptor.handle(message);
        }
        return message;
    }

}
