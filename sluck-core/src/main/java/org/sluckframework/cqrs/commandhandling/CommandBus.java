package org.sluckframework.cqrs.commandhandling;

/**
 * 命令总线，任何时间一个命令只有一个命令处理器处理
 * 
 * @author sunxy
 * @time 2015年9月7日 上午9:30:49	
 * @since 1.0
 */
public interface CommandBus {

    /**
     * 转发命令给处理器
     *
     * @param command The Command to dispatch
     */
    void dispatch(Command<?> command);

    /**
     * 转发命令给处理器，并在命令执行完成后，执行给定的回调函数
     *
     * @param command  The Command to dispatch
     * @param callback The callback to invoke when command processing is complete
     */
    <R> void dispatch(Command<?> command, CommandCallback<R> callback);

    /**
     * 注册 处理 指定的 命令类型的 处理器
     *
     * @param commandName The name of the command to subscribe the handler to
     * @param handler     The handler instance that handles the given type of command
     * @param <C>         The Type of command
     */
    <C> void subscribe(String commandName, CommandHandler<? super C> handler);

    /**
     * 取消 注册 的 指定的 命令处理器
     * 
     * @param commandName The name of the command the handler is subscribed to
     * @param handler     The handler instance to unsubscribe from the CommandBus
     * @param <C>         The Type of command
     * @return <code>true</code> of this handler is successfully unsubscribed, <code>false</code> of the given
     *         <code>handler</code> was not the current handler for given <code>commandType</code>.
     */
    <C> boolean unsubscribe(String commandName, CommandHandler<? super C> handler);

}
