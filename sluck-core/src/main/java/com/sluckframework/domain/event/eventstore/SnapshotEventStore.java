package com.sluckframework.domain.event.eventstore;

import com.sluckframework.domain.event.aggregate.AggregateEvent;

/**
 * 实现接口需要能够存储快照事件，加载事件的时候需要使用快照，任何使用该接口的 readEvents(String, Object) readEvents(String, AggregateIdentifier)
 * 方法的应返回一个在包含最适合的快照版本事件后的事件流
 * @author sunxy
 * @time 2015年8月29日 上午1:45:25
 * @since 1.0
 */
public interface SnapshotEventStore extends EventStore {
	/**
	 * 将聚合的快照事件加入快照仓储中，用于快照的生成
     *
     * @param type          The type of aggregate the event belongs to
     * @param snapshotEvent The event summarizing one or more domain events for a specific aggregate.
     */
    @SuppressWarnings("rawtypes")
	void appendSnapshotEvent(String type, AggregateEvent snapshotEvent);

}
