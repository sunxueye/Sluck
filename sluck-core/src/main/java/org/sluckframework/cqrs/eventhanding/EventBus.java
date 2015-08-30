package org.sluckframework.cqrs.eventhanding;

import org.sluckframework.domain.event.EventProxy;


/**
 * 事件总线 ，用于发布事件  注册事件订阅者
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:48:35
 * @since 1.0
 */
public interface EventBus {
	
	/**
	 * 发布事件
     * @param events The collection of events to publish
     */
    void publish(EventProxy<?>... events);

    /**
     * 增加事件 订阅者 （监听）
     *
     * @param eventListener The event listener to subscribe
     */
    void subscribe(EventListener eventListener);

    /**
     * 取消订阅者
     * 
     * @param eventListener The event listener to unsubscribe
     */
    void unsubscribe(EventListener eventListener);

}
