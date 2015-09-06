package org.sluckframework.cqrs.eventhanding;

import java.util.List;

/**
 * simpleEventBus监视
 * 
 * @author sunxy
 * @time 2015年9月6日 下午8:52:19
 * @since 1.0
 */
public interface SimpleEventBusStatisticsMXBean {

    /**
     * 返回注册的监听器的数量
     *
     * @return long representing the amount of registered listeners
     */
    long getListenerCount();

    /**
     * 返回注册监听器的类型 class name
     *
     * @return List of string representing the names of the registered listeners
     */
    List<String> getListenerTypes();

    /**
     * 返回 接受 到的 事件 数量
     *
     * @return long representing the amount of received events
     */
    long getReceivedEventsCount();

    /**
     * 重置 事件 的数量
     * resets the amount of events received.
     */
    void resetReceivedEventsCount();

}
