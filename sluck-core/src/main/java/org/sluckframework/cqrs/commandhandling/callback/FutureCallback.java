package org.sluckframework.cqrs.commandhandling.callback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.sluckframework.cqrs.commandhandling.CommandCallback;
import org.sluckframework.cqrs.commandhandling.CommandExecutionException;

/**
 * 让 dispatch 的线程等待 执行的结果， 当使用异步 command bus 的时候  需要同步返回额时候使用，内部使用 countDownLatch 实现
 * 
 * @author sunxy
 * @time 2015年9月7日 上午11:50:30	
 * @since 1.0
 */
public class FutureCallback<R> implements CommandCallback<R>, Future<R> {

    private volatile R result;
    private volatile Throwable failure;

    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onSuccess(R executionResult) {
        this.result = executionResult;
        latch.countDown();
    }

    @Override
    public void onFailure(Throwable cause) {
        this.failure = cause;
        latch.countDown();
    }

    /**
     * 如果 CountDownLatch 的数量 不为0 继续等待
     *
     * @return the result of the command handler execution.
     */
    @Override
    public R get() throws InterruptedException, ExecutionException {
        if (!isDone()) {
            latch.await();
        }
        return getFutureResult();
    }

    /**
     * 等待指定时间  如果命令在指定时间内未返回 则抛出异常
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the result of the command handler execution.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws TimeoutException     if the wait timed out
     * @throws ExecutionException   if the command handler threw an exception
     */
    @Override
    public R get(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException {
        if (!isDone() && !latch.await(timeout, unit)) {
            throw new TimeoutException("A Timeout occurred while waiting for a Command Callback");
        }
        return getFutureResult();
    }

    /**
     * 获取 命令结果，如果没有处理完则一直等待
     * 
     * @return the result of the command handler execution.
     */
    public R getResult() {
        if (!isDone()) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return doGetResult();
    }

    /**
     * 获取 命令结果，如果没有处理完则等待指定时间，否则中断处理，返回Null
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the result of the command handler execution.
     */
    public R getResult(long timeout, TimeUnit unit) {
        if (!awaitCompletion(timeout, unit)) {
            return null;
        }
        return doGetResult();
    }

    /**
     * 在指定时间内 是否处理 完毕
     * 
     * @param timeout The amount of time to wait for command processing to complete
     * @param unit    The unit in which the timeout is expressed
     * @return <code>true</code> if command processing completed before the timeout expired, otherwise
     *         <code>false</code>.
     */
    public boolean awaitCompletion(long timeout, TimeUnit unit) {
        try {
            return isDone() || latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 命令执行后 不能 被取消
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing the command should be interrupted; otherwise,
     *                              in-progress tasks are allowed to complete
     * @return <code>false</code>
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * command handler 是否处理完毕
     *
     * @return <code>true</code> if command handler execution has finished, otherwise <code>false</code>.
     */
    @Override
    public boolean isDone() {
        return latch.getCount() == 0L;
    }

    private R doGetResult() {
        if (failure != null) {
            if (failure instanceof Error) {
                throw (Error) failure;
            } else if (failure instanceof RuntimeException) {
                throw (RuntimeException) failure;
            } else {
                throw new CommandExecutionException("An exception occurred while executing a command", failure);
            }
        } else {
            return result;
        }
    }

    private R getFutureResult() throws ExecutionException {
        if (failure != null) {
            throw new ExecutionException(failure);
        } else {
            return result;
        }
    }

}
