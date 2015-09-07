package org.sluckframework.cqrs.commandhandling.gateway;

import java.util.List;

import org.sluckframework.cqrs.commandhandling.Command;

/**
 * 当命令执行过程中发生异常后的 重试机制
 * 
 * @author sunxy
 * @time 2015年9月7日 下午2:41:50	
 * @since 1.0
 */
public interface RetryScheduler {
	
    /**
     * 检查 由于 lastFailure异常 失败的命令，而后判断是否重试
     * 如果重试返回ture，且不执行 原始的 callback，直接执行 commandDispatch
     * 如果不重试返回false, 执行callback
     *
     * @param commandMessage  The Command Message being dispatched
     * @param lastFailure     The last failure recorded for this command
     * @param failures        A condensed view of all known failures of this command. Each element in the array
     *                        represents the cause of the element preceding it.
     * @param commandDispatch The task to be executed to retry a command
     * @return <code>true</code> if the command has been rescheduled, otherwise <code>false</code>
     */
    @SuppressWarnings("rawtypes")
	boolean scheduleRetry(Command command, RuntimeException lastFailure,
                          List<Class<? extends Throwable>[]> failures, Runnable commandDispatch);

}
