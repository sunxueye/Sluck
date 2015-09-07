package org.sluckframework.cqrs.commandhandling;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * simple cb 监视器的实现
 * 
 * @author sunxy
 * @time 2015年9月7日 上午9:52:44	
 * @since 1.0
 */
public class SimpleCommandBusStatistics implements SimpleCommandBusStatisticsMXBean {

    private final AtomicLong handlerCounter = new AtomicLong(0);
    private final AtomicLong receivedCommandCounter = new AtomicLong(0);
    private final List<String> handlerTypes = new CopyOnWriteArrayList<String>();

    public SimpleCommandBusStatistics() {
    }

    /**
     * 处理器的数量
     *
     * @return long representing the amount of registered handlers
     */
    @Override
    public long getCommandHandlerCount() {
        return handlerCounter.get();
    }

    /**
     * 接受到命令的数量
     *
     * @return long representing the amount of received commands
     */
    @Override
    public long getReceivedCommandCount() {
        return receivedCommandCounter.get();
    }

    /**
     * 处理器名称
     *
     * @return List of strings with the names of the registered handlers
     */
    @Override
    public List<String> getHandlerTypes() {
        return Collections.unmodifiableList(handlerTypes);
    }

    /**
     * 重置接受到命令的数量
     */
    @Override
    public void resetReceivedCommandsCounter() {
        receivedCommandCounter.set(0);
    }

    /**
     * 提示监视器 加入新的 处理器 （使用处理器名称）
     *
     * @param name String representing the name of the handler to register
     */
    void reportHandlerRegistered(String name) {
        this.handlerTypes.add(name);
        this.handlerCounter.incrementAndGet();
    }

    /**
     * 告诉监视器 取消注册 指定的 处理器 （使用名称）
     *
     * @param name String representing the name of the handler to un-register
     */
    void recordUnregisteredHandler(String name) {
        this.handlerTypes.remove(name);
        this.handlerCounter.decrementAndGet();
    }

    /**
     * 告诉监视器 接受到一条新的命令
     */
    void recordReceivedCommand() {
        receivedCommandCounter.incrementAndGet();
    }
}
