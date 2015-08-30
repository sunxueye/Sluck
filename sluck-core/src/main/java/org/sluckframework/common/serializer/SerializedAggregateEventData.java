package org.sluckframework.common.serializer;

import org.joda.time.DateTime;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 获取被序列化聚合事件的信息
 * 
 * @author sunxy
 * @time 2015年8月29日 下午10:35:34
 * @since 1.0
 */
public interface SerializedAggregateEventData<T> {
	
	/**
	 * 返回事件的标示符
	 * 
     * @return the identifier of the serialized event
     */
    Object getEventIdentifier();

    /**
     * 返回聚合的标示符
     *
     * @return the Identifier of the Aggregate to which the Event was applied
     */
    Identifier<?> getAggregateIdentifier();

    /**
     * 获取聚合事件的 sequence number
     *
     * @return the sequence number of the event in the aggregate
     */
    long getSequenceNumber();

    /**
     * 获取 聚合事件的 创建时间
     *
     * @return the timestamp at which the event was first created
     */
    DateTime getTimestamp();

    /**
     * 返回 eventProxy 的 payload 的序列化对对象
     *
     * @return the serialized data of the Event's payload
     */
    SerializedObject<T> getPayload();

}
