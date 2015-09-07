package org.sluckframework.cqrs.commandhandling.disruptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.sluckframework.cache.Cache;
import org.sluckframework.cache.NoCache;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.cqrs.commandhandling.CommandDispatchInterceptor;
import org.sluckframework.cqrs.commandhandling.CommandHandlerInterceptor;
import org.sluckframework.cqrs.commandhandling.CommandTargetResolver;
import org.sluckframework.cqrs.commandhandling.RollbackConfiguration;
import org.sluckframework.cqrs.commandhandling.RollbackOnUncheckedExceptionConfiguration;
import org.sluckframework.cqrs.commandhandling.annotation.AnnotationCommandTargetResolver;
import org.sluckframework.cqrs.unitofwork.TransactionManager;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * disruptor的一些配置 信息
 * 
 * @author sunxy
 * @time 2015年9月7日 下午5:52:12	
 * @since 1.0
 */
public class DisruptorConfiguration {

    /**
     * ringBuffer的默认 size, 最好为 2的n次方， 方便取膜运算
     */
    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private int bufferSize;
    private ProducerType producerType;
    private WaitStrategy waitStrategy;
    private Executor executor;
    private RollbackConfiguration rollbackConfiguration;
    private boolean rescheduleCommandsOnCorruptState;
    private long coolingDownPeriod;
    private Cache cache;
    private final List<CommandHandlerInterceptor> invokerInterceptors = new ArrayList<CommandHandlerInterceptor>();
    private final List<CommandHandlerInterceptor> publisherInterceptors = new ArrayList<CommandHandlerInterceptor>();
    private final List<CommandDispatchInterceptor> dispatchInterceptors = new ArrayList<CommandDispatchInterceptor>();
    @SuppressWarnings("rawtypes")
	private TransactionManager transactionManager;
    private CommandTargetResolver commandTargetResolver;
    private int invokerThreadCount = 1;
    private int publisherThreadCount = 1;
    private int serializerThreadCount = 1;
    private Serializer serializer;
    private Class<?> serializedRepresentation = byte[].class;

    /**
     * 使用默认的 4096 size, 性能较低 但比较安全的 blocking wait策略 和 多生产者模式
     */
    public DisruptorConfiguration() {
        this.bufferSize = DEFAULT_BUFFER_SIZE;
        this.producerType = ProducerType.MULTI;
        this.waitStrategy = new BlockingWaitStrategy();
        coolingDownPeriod = 1000;
        cache = NoCache.INSTANCE;
        rescheduleCommandsOnCorruptState = true;
        rollbackConfiguration = new RollbackOnUncheckedExceptionConfiguration();
        commandTargetResolver = new AnnotationCommandTargetResolver();
    }

    /**
     * 当前的等待策略
     *
     * @return the WaitStrategy currently configured
     */
    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    /**
     * 设置 disruptor的等待策略：
     * BusySpinWaitStrategy 提供了 最高的吞吐量，但消耗较高的CPU资源
     * SleepingWaitStrategy 性能较低，但有效的利用了 cpu的资源
     * 默认是 使用BlockingWaitStrategy 折中方案
     * 
     * @param waitStrategy The WaitStrategy to use
     * @return <code>this</code> for method chaining
     *
     * @see com.lmax.disruptor.SleepingWaitStrategy SleepingWaitStrategy
     * @see com.lmax.disruptor.BlockingWaitStrategy BlockingWaitStrategy
     * @see com.lmax.disruptor.BusySpinWaitStrategy BusySpinWaitStrategy
     * @see com.lmax.disruptor.YieldingWaitStrategy YieldingWaitStrategy
     */
    public DisruptorConfiguration setWaitStrategy(WaitStrategy waitStrategy) { //NOSONAR (setter may hide field)
        Assert.notNull(waitStrategy, "waitStrategy must not be null");
        this.waitStrategy = waitStrategy;
        return this;
    }

    /**
     * 返回 使用的 线程池
     *
     * @return the Executor providing the processing resources
     */
    public Executor getExecutor() {
        return executor;
    }

    /**
     * 设置线程池
     * 至少需要3个线程， 一个执行 command 一个 save 事件 ，一个执行 callback ，其余的线程 执行 callback或 recovery操作
     *
     * @param executor the Executor that provides the processing resources
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setExecutor(Executor executor) { //NOSONAR (setter may hide field)
        this.executor = executor;
        return this;
    }

    public List<CommandHandlerInterceptor> getInvokerInterceptors() {
        return invokerInterceptors;
    }

    /**
     * 配置 执行 拦截器
     *
     * @param invokerInterceptors The interceptors to invoke when handling an incoming command
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setInvokerInterceptors(
            List<CommandHandlerInterceptor> invokerInterceptors) {  //NOSONAR (setter may hide field)
        this.invokerInterceptors.clear();
        this.invokerInterceptors.addAll(invokerInterceptors);
        return this;
    }

    public List<CommandHandlerInterceptor> getPublisherInterceptors() {
        return publisherInterceptors;
    }

    public DisruptorConfiguration setPublisherInterceptors(
            List<CommandHandlerInterceptor> publisherInterceptors) { //NOSONAR (setter may hide field)
        this.publisherInterceptors.clear();
        this.publisherInterceptors.addAll(publisherInterceptors);
        return this;
    }

    public List<CommandDispatchInterceptor> getDispatchInterceptors() {
        return dispatchInterceptors;
    }
    
    public DisruptorConfiguration setDispatchInterceptors(
            List<CommandDispatchInterceptor> dispatchInterceptors) { //NOSONAR (setter may hide field)
        this.dispatchInterceptors.clear();
        this.dispatchInterceptors.addAll(dispatchInterceptors);
        return this;
    }

    /**
     * 返回 rollback 配置
     *
     * @return the RollbackConfiguration indicating for the DisruptorCommandBus
     */
    public RollbackConfiguration getRollbackConfiguration() {
        return rollbackConfiguration;
    }

    public DisruptorConfiguration setRollbackConfiguration(
            RollbackConfiguration rollbackConfiguration) { //NOSONAR (setter may hide field)
        Assert.notNull(rollbackConfiguration, "rollbackConfiguration may not be null");
        this.rollbackConfiguration = rollbackConfiguration;
        return this;
    }

    /**
     * 表明 当出现 聚合状态异常时候 是否自动进行重试操作
     *
     * @return <code>true</code> if commands are automatically rescheduled, otherwise <code>false</code>
     */
    public boolean getRescheduleCommandsOnCorruptState() {
        return rescheduleCommandsOnCorruptState;
    }

    /**
     * 设置 当出现 聚合状态异常时候 是否自动进行重试操作
     *
     * @param rescheduleCommandsOnCorruptState
     *         whether or not to automatically reschedule commands that failed due to potentially corrupted aggregate
     *         state.
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setRescheduleCommandsOnCorruptState(
            boolean rescheduleCommandsOnCorruptState) { //NOSONAR (setter may hide field)
        this.rescheduleCommandsOnCorruptState = rescheduleCommandsOnCorruptState;
        return this;
    }

    /**
     * 返回 关闭 commandbus 的关闭时间， 在此期间 将不接受新的命令，但是会执行 已经存在的命令
     * 
     * @return the cooling down period for the shutdown of the DisruptorCommandBus, in milliseconds.
     */
    public long getCoolingDownPeriod() {
        return coolingDownPeriod;
    }

    /**
     * 默认 为 1秒
     *
     * @param coolingDownPeriod the cooling down period for the shutdown of the DisruptorCommandBus, in milliseconds.
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setCoolingDownPeriod(long coolingDownPeriod) { //NOSONAR (setter may hide field)
        this.coolingDownPeriod = coolingDownPeriod;
        return this;
    }

    public Cache getCache() {
        return cache;
    }

    public DisruptorConfiguration setCache(Cache cache) { //NOSONAR (setter may hide field)
        this.cache = cache;
        return this;
    }

    /**
     * 命令解析器
     *
     * @return the CommandTargetResolver that is used to find out which Aggregate is to be invoked for a given Command
     */
    public CommandTargetResolver getCommandTargetResolver() {
        return commandTargetResolver;
    }

    /**
     * 设置 命令参数 解析器， 只有在 serializerThreadCount publisherThreadCount 大于1时候 才使用
     *
     * @param newCommandTargetResolver The CommandTargetResolver to use to indicate which Aggregate instance is target
     *                                 of an incoming Command
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setCommandTargetResolver(CommandTargetResolver newCommandTargetResolver) {
        Assert.notNull(newCommandTargetResolver, "newCommandTargetResolver may not be null");
        this.commandTargetResolver = newCommandTargetResolver;
        return this;
    }

    /**
     * command Handler的线程数量
     *
     * @return the number of threads to use for Command Handler invocation
     */
    public int getInvokerThreadCount() {
        return invokerThreadCount;
    }

    /**
     * 默认为1， 在没有IO操作的情况下，最合理的数量为 [processor count] / 2
     *
     * @param count The number of Threads to use for Command Handler invocation
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setInvokerThreadCount(int count) {
        Assert.isTrue(count > 0, "InvokerCount must be at least 1");
        this.invokerThreadCount = count;
        return this;
    }
    
    public int getPublisherThreadCount() {
        return publisherThreadCount;
    }

    /**
     * 设置 发布和存储 事件的 线程 数量  最合理的数量为 [processor count] / 2
     *
     * @param count The number of Threads to use for publishing
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setPublisherThreadCount(int count) {
        Assert.isTrue(count > 0, "PublisherCount must be at least 1");
        this.publisherThreadCount = count;
        return this;
    }

    /**
     * 返回配置的 序列化 操作的 线程的数量 ，如果没有则忽略
     *
     * @return the number of threads to perform pre-serialization with
     */
    public int getSerializerThreadCount() {
        return serializerThreadCount;
    }

    /**
     * 配置的 序列化 操作的 线程的数量
     * 
     * @param newSerializerThreadCount the number of threads to perform pre-serialization with
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setSerializerThreadCount(int newSerializerThreadCount) {
        Assert.isTrue(newSerializerThreadCount >= 0, "SerializerThreadCount must be >= 0");
        this.serializerThreadCount = newSerializerThreadCount;
        return this;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public DisruptorConfiguration setSerializer(Serializer newSerializer) {
        this.serializer = newSerializer;
        return this;
    }

    /**
     * 判断 是否配置 序列化 配置
     *
     * @return whether pre-serialization is configured
     */
    public boolean isPreSerializationConfigured() {
        return serializer != null && serializerThreadCount > 0;
    }

    /**
     * 返回序列化对象的 中间类型 默认为 字节数组
     *
     * @return the type of data the serialized object should be represented in
     */
    public Class<?> getSerializedRepresentation() {
        return serializedRepresentation;
    }

    /**
     * 配置 序列化对象的 中间类型 
     *
     * @param newSerializedRepresentation the type of data the serialized object should be represented in. May not be
     *                                    <code>null</code>.
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setSerializedRepresentation(Class<?> newSerializedRepresentation) {
        Assert.notNull(newSerializedRepresentation, "Serialized representation may not be null");
        this.serializedRepresentation = newSerializedRepresentation;
        return this;
    }

    @SuppressWarnings("rawtypes")
	public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @SuppressWarnings("rawtypes")
	public DisruptorConfiguration setTransactionManager(TransactionManager newTransactionManager) {
        this.transactionManager = newTransactionManager;
        return this;
    }

    /**
     * ringBuffer size 2的n方 性能最高
     *
     * @return the buffer size to use.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the buffer size to use.
     * The default is 4096.
     *
     * @param newBufferSize the buffer size to use
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setBufferSize(int newBufferSize) {
        this.bufferSize = newBufferSize;
        return this;
    }

    /**
     * 返回 使用的 生产者类型
     *
     * @return the producer type to use.
     */
    public ProducerType getProducerType() {
        return producerType;
    }

    /**
     * 配置 生产者类型，默认为多例
     *
     * @param producerType the producer type to use
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setProducerType(ProducerType producerType) {
        Assert.notNull(producerType, "producerType must not be null");
        this.producerType = producerType;
        return this;
    }

}
