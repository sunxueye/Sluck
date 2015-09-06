package org.sluckframework.cqrs.eventhanding;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * event bus 的监视器， 统计simpleeventbus的相关信息
 * 
 * @author sunxy
 * @time 2015年9月6日 下午8:54:08
 * @since 1.0
 */
public class SimpleEventBusStatistics implements SimpleEventBusStatisticsMXBean {

    private final AtomicLong listenerCount = new AtomicLong(0);
    private final AtomicLong publishedEventCounter = new AtomicLong(0);
    private final List<String> listeners = new CopyOnWriteArrayList<String>();

    SimpleEventBusStatistics() {
    }

    @Override
    public long getListenerCount() {
        return listenerCount.get();
    }

    @Override
    public long getReceivedEventsCount() {
        return publishedEventCounter.get();
    }

    /**
     * 重置 接收到的 事件数量
     */
    @Override
    public void resetReceivedEventsCount() {
        publishedEventCounter.set(0);
    }

    @Override
    public List<String> getListenerTypes() {
        return Collections.unmodifiableList(listeners);
    }

    /*----- end of jmx enabled methods -----*/

    /**
     * 用eventBus 监听器的 name 注册监听器
     *
     * @param name String representing the name of the registered listener
     */
    void listenerRegistered(String name) {
        this.listeners.add(name);
        this.listenerCount.incrementAndGet();
    }

    /**
     * 根据监听器 name Remove 监听器 
     *
     * @param name String representing the name of the listener to un-register.
     */
    void recordUnregisteredListener(String name) {
        this.listeners.remove(name);
        this.listenerCount.decrementAndGet();
    }

    /**
     * 表明一个新的事件的到来
     */
    void recordPublishedEvent() {
        publishedEventCounter.incrementAndGet();
    }
}
