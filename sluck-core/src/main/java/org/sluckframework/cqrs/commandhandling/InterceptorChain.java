package org.sluckframework.cqrs.commandhandling;

/**
 * 拦截器链 执行拦截， 拦截后使用 命令处理器 处理命令
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:02:48	
 * @since 1.0
 */
public interface InterceptorChain {

    /**
     * 单个的拦截器链处理
     *
     * @return The return value of the command execution, if any
     */
    Object proceed() throws Throwable;

    /**
     * 拦截器链处理
     * 
     * @param command The command being executed
     * @return The return value of the command execution, if any
     */
    Object proceed(Command<?> command) throws Throwable;

}
