package org.sluckframework.cqrs.commandhandling.gateway;

import java.util.concurrent.TimeUnit;

import org.sluckframework.cqrs.commandhandling.CommandCallback;

/**
 * 命令网关， 用此网关发送命令，交于应用层使用，对外提供 发送命令 api，内部封装 commanbus 
 * 
 * @author sunxy
 * @time 2015年9月7日 下午2:12:38	
 * @since 1.0
 */
public interface CommandGateway {

    /**
     * 发送命令，使用指定的 callback
     *
     * @param command  The command to dispatch
     * @param callback The callback to notify when the command has been processed
     * @param <R>      The type of result expected from command execution
     */
    <R> void send(Object command, CommandCallback<R> callback);

    /**
     * 发送命令 并 等待结果
     *
     * @param command The command to dispatch
     * @return the result of command execution, or <code>null</code> if the thread was interrupted while waiting for
     *         the command to execute
     */
    <R> R sendAndWait(Object command);

    /**
     * 发送命令并等待指定时间，如果时间到了还没返回，则返回Null
     *
     * @param command The command to dispatch
     * @param timeout The amount of time the thread is allows to wait for the result
     * @param unit    The unit in which <code>timeout</code> is expressed
     * @param <R>     The type of result expected from command execution
     * @return the result of command execution, or <code>null</code> if the thread was interrupted while waiting for
     *         the command to execute
     */
    <R> R sendAndWait(Object command, long timeout, TimeUnit unit);

    /**
     * 发送 命令 并立即返回，不等待执行结果
     *
     * @param command The command to dispatch
     */
    void send(Object command);

}
