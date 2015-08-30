package org.sluckframework.cqrs.upcasting;

import org.joda.time.DateTime;


/** 
 * 接口 提供了 对象转换的 上下文信息， 通常是 包含Payload事件的eventproxy的信息
 * 
 * @author sunxy
 * @time 2015年8月29日 下午5:57:27
 * @since 1.0
 */
public interface UpcastingContext {
	
	/**
	 * 返回将被upcast事件的标示符
     *
     * @return the identifier of the message wrapping the object to upcast
     */
    String getEventIdentifier();

    /**
     * 返回事件对应的聚合的标示符，如果不是一个聚合事件，那么返回 null
     * 
     * @return the Identifier of the Aggregate to which the Event was applied, or null if not applicable
     */
    Object getAggregateIdentifier();

    /**
     * 返回事件的 sequence number
     *
     * @return the sequence number of the event in the aggregate, if available
     */
    Long getSequenceNumber();

    /**
     * 返回事件的创建时间
     * 
     * @return the timestamp at which the event was first created, if available
     */
    DateTime getTimestamp();

}
