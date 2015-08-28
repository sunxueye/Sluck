package com.sluckframework.domain.aggregate;

import com.sluckframework.domain.event.aggregate.AggregateEvent;


/**
 * 聚合 注册 (eventContainer) 需要被发布的事件的时候 回调函数
 * 可以使用该回调函数在事件处理器处理之前改变需要发布的事件
 * 
 * @author sunxy
 * @time 2015年8月28日 下午4:23:22	
 * @since 1.0
 */
public interface EventRegistrationCallback {
	
	/**
	 * 回调函数的执行 - 函数对象
	 * 
     * @param event The event registered for publication
     * @return the message to actually publish. 
     */
    @SuppressWarnings("rawtypes")
	AggregateEvent onRegisteredEvent(AggregateEvent event);

}
