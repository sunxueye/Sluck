package org.sluckframework.cqrs.commandhandling;

import org.sluckframework.cqrs.unitofwork.UnitOfWork;


/**
 * 命令处理 的拦截器，可以在指定的 操作之前 自定义一些操作
 * 
 * @author sunxy
 * @time 2015年9月7日 上午9:56:33	
 * @since 1.0
 */
public interface CommandHandlerInterceptor {

    /**
     * 每个即将转发的 命令都会被 拦截器处理，拦截器通过 uow 获取命令的相关信息
     * 使用拦截器链 来执行 拦截器
     *
     * @param commandMessage   The command being dispatched
     * @param unitOfWork       The UnitOfWork in which
     * @param interceptorChain The interceptor chain that allows this interceptor to proceed the dispatch process
     * @return the result of the command handler. May have been modified by interceptors.
     */
    Object handle(Command<?> commandMessage, UnitOfWork unitOfWork, InterceptorChain interceptorChain)
            throws Throwable;

}
