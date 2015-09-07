package org.sluckframework.cqrs.commandhandling;


/**
 * dispatch 命令时候 拦截器
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:04:29	
 * @since 1.0
 */
public interface CommandDispatchInterceptor {
	
	/**
	 * 当命令 被 dispatch的时候 执行
	 * 
     * @param Command The command message intended to be dispatched on the Command Bus
     * @return the command message to dispatch on the Command Bus
     */
    Command<?> handle(Command<?> Command);

}
