package org.sluckframework.cqrs.eventhanding.async;

import java.util.concurrent.TimeUnit;

/**
 * 重试机制告诉 eventProcessor 应该怎么样处理错误的事务,
 *
 * Author: sunxy
 * Created: 2015-09-15 22:12
 * Since: 1.0
 */
public abstract class RetryPolicy {

    private static final RetryPolicy PROCEED = new SimpleRetryPolicy(0, false, false);
    private static final RetryPolicy SKIP = new SimpleRetryPolicy(0, true, false);

    /**
     * 告诉调度者忽略失败继续执行,这个事件将被集群中的其他事件监听器处理
     *
     * @return A RetryPolicy instance requesting the scheduler to proceed with dispatching
     */
    public static RetryPolicy proceed() {
        return PROCEED;
    }

    /**
     * 回滚 uow and 跳过事件,其他事件处理器将不会处理事件
     *
     * @return A RetryPolicy instance requesting the scheduler to rollback the Unit of Work and continue processing the
     * next Event.
     */
    public static RetryPolicy skip() {
        return SKIP;
    }

    /**
     * 回滚 row 并重试, 此事件将步会被其他的事件处理器处理
     *
     * @param timeout The amount of time to wait before retrying
     * @param unit    The unit of time for the timeout
     * @return a RetryPolicy requesting a rollback and a retry of the failed Event after the given timeout
     */
    public static RetryPolicy retryAfter(long timeout, TimeUnit unit) {
        return new SimpleRetryPolicy(unit.toMillis(timeout), true, true);
    }

    /**
     * 返回 调度者 等待的 time
     *
     * @return the amount of time, in milliseconds, the scheduler should wait before continuing processing.
     */
    public abstract long waitTime();

    /**
     * 标示 调度者 是否重新尝试处理失败的事件
     *
     * @return <code>true</code> if the scheduler should reschedule the failed event, otherwise <code>false</code>
     */
    public abstract boolean requiresRescheduleEvent();

    /**
     * 是否回滚 uow
     *
     * @return <code>true</code> to indicate the scheduler should perform a rollback or <code>false</code> to request a
     * commit.
     */
    public abstract boolean requiresRollback();

    private static final class SimpleRetryPolicy extends RetryPolicy {

        private final long waitTime;
        private final boolean rollback;
        private final boolean rescheduleEvent;

        private SimpleRetryPolicy(long waitTime, boolean rollback, boolean rescheduleEvent) {
            this.waitTime = waitTime;
            this.rollback = rollback;
            this.rescheduleEvent = rescheduleEvent;
        }

        @Override
        public long waitTime() {
            return waitTime;
        }

        @Override
        public boolean requiresRescheduleEvent() {
            return rescheduleEvent;
        }

        @Override
        public boolean requiresRollback() {
            return rollback;
        }
    }
}