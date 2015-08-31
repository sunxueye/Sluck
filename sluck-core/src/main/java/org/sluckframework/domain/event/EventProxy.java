package org.sluckframework.domain.event;

import java.io.Serializable;

import org.joda.time.DateTime;


/**
 * 领域内由于某些操作 产生的事件
 * 
 * @author sunxy
 * @time 2015年8月28日 下午3:11:53	
 * @since 1.0
 */
public interface EventProxy<T> extends Serializable {
	
	/**
	 * 获取 事件 标识符
	 * 
	 * @return id
	 */
	Object getIdentifier();
	
	/**
     * 承载的客户端定义的真正的事件
     *
     * @return the payload of this Event
     */
    T getPayload();
	
	/**
	 * 获取事件的类型 / 可能 有延迟 反序列对象 所以 payloadType 不一定就是 T
	 * 
	 * @return event type
	 */
	Class<?> getPayloadType();
	
	 /**
     * 事件的发生时间
     * 
     * @return Date of this event.
     */
    DateTime occurredOn();

}
