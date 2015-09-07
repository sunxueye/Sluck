package org.sluckframework.cqrs.commandhandling;

import java.util.Iterator;

import org.sluckframework.cqrs.unitofwork.UnitOfWork;

/**
 * 默认的拦截器链
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:17:51	
 * @since 1.0
 */
public class DefaultInterceptorChain implements InterceptorChain {

    private Command<?> command;
    @SuppressWarnings("rawtypes")
	private final CommandHandler handler;
    private final Iterator<? extends CommandHandlerInterceptor> chain;
    private final UnitOfWork unitOfWork;

    public DefaultInterceptorChain(Command<?> command, UnitOfWork unitOfWork, CommandHandler<?> handler,
                                   Iterable<? extends CommandHandlerInterceptor> chain) {
        this.command = command;
        this.handler = handler;
        this.chain = chain.iterator();
        this.unitOfWork = unitOfWork;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public Object proceed(Command<?> commandProceedWith) throws Throwable {
        command = commandProceedWith;
        if (chain.hasNext()) {
            return chain.next().handle(commandProceedWith, unitOfWork, this);
        } else {
            return handler.handle(commandProceedWith, unitOfWork);
        }
    }

    @Override
    public Object proceed() throws Throwable {
        return proceed(command);
    }

}
