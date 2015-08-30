package org.sluckframework.cqrs.eventhanding;

import org.sluckframework.domain.event.EventProxy;


/**
 * 事件监听者 监听 处理事件
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:50:23
 * @since 1.0
 */
public interface EventListener {
	
	/**
	 * handle the event
	 * 
     * @param event the event to handle
     */
	void handle(EventProxy<?> event);

}
