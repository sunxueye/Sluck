package com.sluckframework.domain.event.eventstore;

import com.sluckframework.domain.event.aggregate.AggregateEventStream;
import com.sluckframework.domain.identifier.Identifier;

/**
 * 事件存储，从存储中读取事件流，支持根据事件 sequence 读取部分事件流
 * 
 * @author sunxy
 * @time 2015年8月29日 上午1:37:37
 * @since 1.0
 */
public interface EventStore {
	
	 /**
     * 将指定的事件流中的事件添加到 eventStore中
     *
     * @param type   The type descriptor of the object to store
     * @param events The event stream containing the events to store
     * @throws EventStoreException if an error occurs while storing the events in the event stream
     */
    void appendEvents(String type, AggregateEventStream events);

    /**
     * 根据聚合根读取事件流，用于聚合的重塑，实现可以使用 snapshot events 来快速重塑聚合
     * 
     * @param type       The type descriptor of the object to retrieve
     * @param identifier The unique aggregate identifier of the events to load
     * @return an event stream containing the events of the aggregate
     *
     * @throws EventStoreException if an error occurs while reading the events in the event stream
     */
    AggregateEventStream readEvents(String type, Identifier<?> identifier);
    
    /**
     * 根据聚合标示符 读取从 firstSequenceNumber 开始的事件流
     *
     * @param type                The type identifier of the aggregate
     * @param identifier          The identifier of the aggregate
     * @param firstSequenceNumber The sequence number of the first event to find
     * @return a Stream containing events for the given aggregate, starting at the given first sequence number
     */
    AggregateEventStream readEvents(String type, Identifier<?> identifier, long firstSequenceNumber);

    /**
     * 根据聚合标示符，事件的开始sequence 和 结束 sequnce读取事件流，如果 结束sequnce对应的事件不存在，则一直读取到最后的事件
     * 
     * @param type                The type identifier of the aggregate
     * @param identifier          The identifier of the aggregate
     * @param firstSequenceNumber The sequence number of the first event to find
     * @param lastSequenceNumber  The sequence number of the last event in the stream
     * @return a Stream containing events for the given aggregate
     */
    AggregateEventStream readEvents(String type, Identifier<?> identifier, long firstSequenceNumber, long lastSequenceNumber);

}
