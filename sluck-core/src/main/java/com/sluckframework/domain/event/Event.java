package com.sluckframework.domain.event;

import java.io.Serializable;

import org.joda.time.DateTime;


/**
 * 领域内由于某些操作 产生的事件
 * 
 * @author sunxy
 * @time 2015年8月28日 下午3:11:53	
 * @since 1.0
 */
public interface Event<T, ID extends Object> extends Serializable {
	
	/**
	 * 获取 事件 标识符
	 * 
	 * @return id
	 */
	ID getIdentifier();
	
	/**
	 * 获取事件的类型
	 * 
	 * @return event type
	 */
	Class<T> getPayloadType();
	
	 /**
     * 事件的发生时间
     * 
     * @return Date of this event.
     */
    DateTime occurredOn();

}
