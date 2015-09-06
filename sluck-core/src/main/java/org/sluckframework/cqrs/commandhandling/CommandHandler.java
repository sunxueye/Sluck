package org.sluckframework.cqrs.commandhandling;

import org.sluckframework.cqrs.unitofwork.UnitOfWork;


/**
 * 命令处理器
 * 
 * @author sunxy
 * @time 2015年9月6日 下午9:34:58
 * @since 1.0
 */
public interface CommandHandler<T> {

    /**
     * 处理命令
     *
     * @param Command The command to process.
     * @param unitOfWork     The UnitOfWork the command is processed in
     * @return The result of the command processing, if any.
     *
     */
    Object handle(Command<T> command, UnitOfWork unitOfWork) throws Throwable;

}
