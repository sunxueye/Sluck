package org.sluckframework.cqrs.commandhandling.gateway;

import java.util.ArrayList;
import java.util.List;

import org.sluckframework.common.lock.DeadlockException;
import org.sluckframework.cqrs.commandhandling.CommandBus;
import org.sluckframework.cqrs.commandhandling.CommandCallback;
import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;

/**
 * 带有重试机制的 callback
 * 
 * @author sunxy
 * @time 2015年9月7日 下午3:23:32	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class RetryingCallback<R> implements CommandCallback<R> {

    private final CommandCallback<R> delegate;
	private final Command Command;
    private final RetryScheduler retryScheduler;
    private final CommandBus commandBus;
    private final List<Class<? extends Throwable>[]> history;
    private final Runnable dispatchCommand;

    /**
     * 使用指定 委派 callback 和 命令 重试机制 commonbus 初始化
     *
     * @param delegate       The callback to invoke when the command succeeds, or when retries are rejected.
     * @param Command The message being dispatched
     * @param retryScheduler The scheduler that decides if and when a retry should be scheduled
     * @param commandBus     The commandBus on which the command must be dispatched
     */
    public RetryingCallback(CommandCallback<R> delegate, Command Command, RetryScheduler retryScheduler,
                            CommandBus commandBus) {
        this.delegate = delegate;
        this.Command = Command;
        this.retryScheduler = retryScheduler;
        this.commandBus = commandBus;
        this.history = new ArrayList<Class<? extends Throwable>[]>();
        this.dispatchCommand = new RetryDispatch();
    }

    @Override
    public void onSuccess(R result) {
        delegate.onSuccess(result);
    }

    @Override
    public void onFailure(Throwable cause) {
        history.add(simplify(cause));
        try {
            // we fail immediately when the exception is checked,
            // or when it is a Deadlock Exception and we have an active unit of work
            if (!(cause instanceof RuntimeException)
                    || (isCausedBy(cause, DeadlockException.class) && CurrentUnitOfWork.isStarted())
                    || !retryScheduler.scheduleRetry(Command, (RuntimeException) cause,
                                                     new ArrayList<Class<? extends Throwable>[]>(history),
                                                     dispatchCommand)) {
                delegate.onFailure(cause);
            }
        } catch (Exception e) {
            delegate.onFailure(e);
        }
    }

    private boolean isCausedBy(Throwable exception, Class<? extends Throwable> causeType) {
        return causeType.isInstance(exception)
                || (exception.getCause() != null && isCausedBy(exception.getCause(), causeType));
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] simplify(Throwable cause) {
        List<Class<? extends Throwable>> types = new ArrayList<Class<? extends Throwable>>();
        types.add(cause.getClass());
        Throwable rootCause = cause;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
            types.add(rootCause.getClass());
        }
        return types.toArray(new Class[types.size()]);
    }

    private class RetryDispatch implements Runnable {

        @Override
        public void run() {
            try {
                commandBus.dispatch(Command, RetryingCallback.this);
            } catch (Exception e) {
                RetryingCallback.this.onFailure(e);
            }
        }
    }
}
