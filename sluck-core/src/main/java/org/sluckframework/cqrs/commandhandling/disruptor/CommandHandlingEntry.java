package org.sluckframework.cqrs.commandhandling.disruptor;

import java.util.List;

import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandHandler;
import org.sluckframework.cqrs.commandhandling.CommandHandlerInterceptor;
import org.sluckframework.cqrs.commandhandling.DefaultInterceptorChain;
import org.sluckframework.cqrs.commandhandling.InterceptorChain;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.domain.identifier.Identifier;

import com.lmax.disruptor.EventFactory;

/**
 * disruptor中的 entity, 封装了 命令 和命令处理器， 各种拦截器信息，由 消费端 执行
 * 
 * @author sunxy
 * @time 2015年9月7日 下午4:08:36	
 * @since 1.0
 */
public class CommandHandlingEntry {

    private final CommandHandler<Object> repeatingCommandHandler;
    private Command<?> command;
    private InterceptorChain invocationInterceptorChain;
    private InterceptorChain publisherInterceptorChain;
    private DisruptorUnitOfWork unitOfWork;
    private Throwable exceptionResult;
    private Object result;
    private int publisherSegmentId;
    @SuppressWarnings("rawtypes")
	private BlacklistDetectingCallback callback;
    // for recovery of corrupt aggregates
    private boolean isRecoverEntry;
    private Identifier<?> aggregateIdentifier;
    private int invokerSegmentId;
    private int serializerSegmentId;
    private final boolean transactional;

    /**
     * 初始化
     * 
     * @param transactional Whether this entry contains transactional Unit of Work
     */
    public CommandHandlingEntry(boolean transactional) {
        this.transactional = transactional;
        repeatingCommandHandler = new RepeatingCommandHandler();
    }

    /**
     * 将被执行的命令
     *
     * @return the Command to be executed
     */
    public Command<?> getCommand() {
        return command;
    }

    /**
     * 返回拦截器链
     *
     * @return the InterceptorChain for the invocation process registered with this entry
     */
    public InterceptorChain getInvocationInterceptorChain() {
        return invocationInterceptorChain;
    }

    /**
     * 返回 publish 拦截器链
     *
     * @return the InterceptorChain for the publication process registered with this entry
     */
    public InterceptorChain getPublisherInterceptorChain() {
        return publisherInterceptorChain;
    }

    public DisruptorUnitOfWork getUnitOfWork() {
        return unitOfWork;
    }

    /**
     * 注册 在 执行命令过程中 产生的异常
     *
     * @param exceptionResult the exception that occurred while processing the incoming command
     */
    public void setExceptionResult(Throwable exceptionResult) {
        this.exceptionResult = exceptionResult;
    }
    
    public Throwable getExceptionResult() {
        return exceptionResult;
    }

    /**
     * 注册 命令结果
     *
     * @param result the result of the command's execution, if successful
     */
    public void setResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    /**
     * 返回 黑名单 callback
     *
     * @return the CommandCallback instance for the executed command
     */
    @SuppressWarnings("rawtypes")
	public BlacklistDetectingCallback getCallback() {
        return callback;
    }

    /**
     * 表明 是否是 恢复/重试 的 聚合 实体，如果是， 则不包含命令 处理信息
     *
     * @return <code>true</code> if this entry represents a recovery request, otherwise <code>false</code>.
     */
    public boolean isRecoverEntry() {
        return isRecoverEntry;
    }

    /**
     * Returns the identifier of the aggregate to recover. Returns <code>null</code> when {@link #isRecoverEntry()}
     * returns <code>false</code>.
     *
     * @return the identifier of the aggregate to recover
     */
    public Identifier<?> getAggregateIdentifier() {
        return aggregateIdentifier;
    }

    public int getInvokerId() {
        return invokerSegmentId;
    }

    /**
     * 返回 serializer 线程的路由 id
     *
     * @return the Segment ID that identifies the serializer thread to process this entry
     */
    public int getSerializerSegmentId() {
        return serializerSegmentId;
    }

    /**
     * 返回 publisher线程 的路由ID
     *
     * @return the Identifier of the publisher that is chosen to handle this entry
     */
    public int getPublisherId() {
        return publisherSegmentId;
    }

    /**
     * 重置 entry， 准备给下一个 命令 使用
     *
     * @param newCommand             The new command the entry is used for
     * @param newCommandHandler      The Command Handler responsible for handling <code>newCommand</code>
     * @param newInvokerSegmentId    The SegmentID of the invoker that should process this entry
     * @param newPublisherSegmentId  The SegmentID of the publisher that should process this entry
     * @param newSerializerSegmentId The SegmentID of the serializer that should process this entry
     * @param newCallback            The callback to report the result of command execution to
     * @param invokerInterceptors    The interceptors to invoke during the command handler invocation phase
     * @param publisherInterceptors  The interceptors to invoke during the publication phase
     */
    @SuppressWarnings("rawtypes")
	public void reset(Command<?> newCommand, CommandHandler newCommandHandler, // NOSONAR - Not important
                      int newInvokerSegmentId, int newPublisherSegmentId, int newSerializerSegmentId,
                      BlacklistDetectingCallback newCallback, List<CommandHandlerInterceptor> invokerInterceptors,
                      List<CommandHandlerInterceptor> publisherInterceptors) {
        this.command = newCommand;
        this.invokerSegmentId = newInvokerSegmentId;
        this.publisherSegmentId = newPublisherSegmentId;
        this.serializerSegmentId = newSerializerSegmentId;
        this.callback = newCallback;
        this.isRecoverEntry = false;
        this.aggregateIdentifier = null;
        this.result = null;
        this.exceptionResult = null;
        this.unitOfWork = new DisruptorUnitOfWork(transactional);
        this.invocationInterceptorChain = new DefaultInterceptorChain(newCommand,
                                                                      unitOfWork,
                                                                      newCommandHandler,
                                                                      invokerInterceptors);
        this.publisherInterceptorChain = new DefaultInterceptorChain(newCommand,
                                                                     unitOfWork,
                                                                     repeatingCommandHandler,
                                                                     publisherInterceptors);
    }

    /**
     * 重置实体， 标识为 recovery实体
     *
     * @param newAggregateIdentifier The identifier of the aggregate to recover
     */
    public void resetAsRecoverEntry(Identifier<?> newAggregateIdentifier) {
        this.isRecoverEntry = true;
        this.aggregateIdentifier = newAggregateIdentifier;
        this.command = null;
        this.callback = null;
        result = null;
        exceptionResult = null;
        invocationInterceptorChain = null;
        unitOfWork = null;
        invokerSegmentId = -1;
        serializerSegmentId = -1;
    }

    /**
     * Factory class for CommandHandlingEntry instances.
     */
    public static class Factory implements EventFactory<CommandHandlingEntry> {

        private final boolean transactional;

        public Factory(boolean transactional) {
            this.transactional = transactional;
        }

        @Override
        public CommandHandlingEntry newInstance() {
            return new CommandHandlingEntry(transactional);
        }
    }

    private class RepeatingCommandHandler implements CommandHandler<Object> {

        @Override
        public Object handle(Command<Object> Command, UnitOfWork uow) throws Throwable {
            if (exceptionResult != null) {
                throw exceptionResult;
            }
            return result;
        }
    }

}
