package org.sluckframework.cqrs.commandhandling.gateway;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.sluckframework.cqrs.commandhandling.CommandBus;
import org.sluckframework.cqrs.commandhandling.CommandCallback;
import org.sluckframework.cqrs.commandhandling.CommandDispatchInterceptor;
import org.sluckframework.cqrs.commandhandling.callback.FutureCallback;
import org.sluckframework.cqrs.commandhandling.callback.NoOpCallback;

/**
 * commandGateWay的默认实现，可以配置 重试机制 和 dispatch拦截器
 * 是否立即返回 执行  取决于 同步还是异步 的commandBus 和 callback的类型
 * 
 * @author sunxy
 * @time 2015年9月7日 下午3:43:12	
 * @since 1.0
 */
public class DefaultCommandGateway extends AbstractCommandGateway {

    /**
     * 使用指定的 commandBus 和 dispatch 拦截器 初始化
     *
     * @param commandBus                  The CommandBus on which to dispatch the Command Messages
     * @param commandDispatchInterceptors The interceptors to invoke before dispatching commands to the Command Bus
     */
    public DefaultCommandGateway(CommandBus commandBus, CommandDispatchInterceptor... commandDispatchInterceptors) {
        this(commandBus, null, commandDispatchInterceptors);
    }

    /**
     * 使用指定的 commandBus 和 dispatch 拦截器  和重试机制 初始化
     *
     * @param commandBus                  The CommandBus on which to dispatch the Command Messages
     * @param retryScheduler              The scheduler that will decide whether to reschedule commands
     * @param commandDispatchInterceptors The interceptors to invoke before dispatching commands to the Command Bus
     */
    public DefaultCommandGateway(CommandBus commandBus, RetryScheduler retryScheduler,
                                 CommandDispatchInterceptor... commandDispatchInterceptors) {
        this(commandBus, retryScheduler, asList(commandDispatchInterceptors));
    }

    /**
     * 使用指定的 commandBus 和 dispatch 拦截器  和重试机制 初始化
     *
     * @param commandBus                  The CommandBus on which to dispatch the Command Messages
     * @param retryScheduler              The scheduler that will decide whether to reschedule commands
     * @param commandDispatchInterceptors The interceptors to invoke before dispatching commands to the Command Bus
     */
    public DefaultCommandGateway(CommandBus commandBus, RetryScheduler retryScheduler,
                                 List<CommandDispatchInterceptor> commandDispatchInterceptors) {
        super(commandBus, retryScheduler, commandDispatchInterceptors);
    }

    @Override
    public <R> void send(Object command, CommandCallback<R> callback) {
        super.send(command, callback);
    }

    /**
     * 发送命令 并 等待返回结果
     *
     * @param command The command to send
     * @param <R>     The expected type of return value
     * @return The result of the command handler execution
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R> R sendAndWait(Object command) {
        FutureCallback<Object> futureCallback = new FutureCallback<Object>();
        send(command, futureCallback);
        return (R) futureCallback.getResult();
    }

    /**
     * 发送命令 并在指定时间内 等待返回结果，否则抛出异常
     *
     * @param command The command to send
     * @param timeout The maximum time to wait
     * @param unit    The time unit of the timeout argument
     * @param <R>     The expected type of return value
     * @return The result of the command handler execution
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R> R sendAndWait(Object command, long timeout, TimeUnit unit) {
        FutureCallback<Object> futureCallback = new FutureCallback<Object>();
        send(command, futureCallback);
        return (R) futureCallback.getResult(timeout, unit);
    }

    /**
     * 发送命令 不执行callback
     *
     * @param command The command to send
     */
    @Override
    public void send(Object command) {
        send(command, new NoOpCallback());
    }

}
