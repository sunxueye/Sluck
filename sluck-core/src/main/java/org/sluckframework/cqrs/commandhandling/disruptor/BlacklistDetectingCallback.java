package org.sluckframework.cqrs.commandhandling.disruptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandCallback;

import com.lmax.disruptor.RingBuffer;

/**
 * 聚合黑名单 callback,当一个聚合在黑名单中，执行 claanUp 操作
 * 
 * @author sunxy
 * @time 2015年9月7日 下午4:25:44	
 * @since 1.0
 */
public class BlacklistDetectingCallback<R> implements CommandCallback<R> {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistDetectingCallback.class);

    private final CommandCallback<R> delegate;
    @SuppressWarnings("rawtypes")
	private final Command command;
    private final RingBuffer<CommandHandlingEntry> ringBuffer;
    private final DisruptorCommandBus commandBus;
    private final boolean rescheduleOnCorruptState;

    /**
     * 使用 指定 委派 callback 和 命令  等参数 初始化
     *
     * @param delegate                 The callback to invoke when an exception occurred
     * @param command                  The command being executed
     * @param ringBuffer               The RingBuffer on which an Aggregate Cleanup should be scheduled when a
     *                                 corrupted
     *                                 aggregate state was detected
     * @param commandBus               The CommandBus on which the command should be rescheduled if it was executed on
     *                                 a
     *                                 corrupt aggregate
     * @param rescheduleOnCorruptState Whether the command should be retried if it has been executed against corrupt
     *                                 state
     */
    @SuppressWarnings("rawtypes")
	public BlacklistDetectingCallback(CommandCallback<R> delegate, Command command,
                                      RingBuffer<CommandHandlingEntry> ringBuffer,
                                      DisruptorCommandBus commandBus, boolean rescheduleOnCorruptState) {
        this.delegate = delegate;
        this.command = command;
        this.ringBuffer = ringBuffer;
        this.commandBus = commandBus;
        this.rescheduleOnCorruptState = rescheduleOnCorruptState;
    }

    @Override
    public void onSuccess(R result) {
        if (delegate != null) {
            delegate.onSuccess(result);
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        if (cause instanceof AggregateBlacklistedException) {
            long sequence = ringBuffer.next();
            CommandHandlingEntry event = ringBuffer.get(sequence);
            event.resetAsRecoverEntry(((AggregateBlacklistedException) cause).getAggregateIdentifier());
            ringBuffer.publish(sequence);
            if (delegate != null) {
                delegate.onFailure(cause.getCause());
            }
        } else if (rescheduleOnCorruptState && cause instanceof AggregateStateCorruptedException) {
            commandBus.doDispatch(command, delegate);
        } else if (delegate != null) {
            delegate.onFailure(cause);
        } else {
            logger.warn("Command {} resulted in an exception:", command.getPayloadType().getSimpleName(), cause);
        }
    }

    /**
     * 是否包含 委派 callback
     *
     * @return <code>true</code> if this callback has a delegate, otherwise <code>false</code>.
     */
    public boolean hasDelegate() {
        return delegate != null;
    }
}
