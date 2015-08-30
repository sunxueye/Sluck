package org.sluckframework.implement.eventstore.jdbc;

import java.util.Iterator;
import java.util.List;

import org.sluckframework.common.serializer.SerializedAggregateEventData;
import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 事件数据存储
 * 
 * @author sunxy
 * @time 2015年8月29日 下午10:15:06
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public interface EventEntryStore<T> {
	
	/**
	 * 持久化 已序列化的事件到存储
     *
     * @param aggregateType The type identifier of the aggregate that generated the event
     * @param event The actual event instance. 
     * @param serializedPayload  The serialized payload of the event
     */
	void persistEvent(String aggregateType, AggregateEvent event, SerializedObject<T> serializedPayload);

    /**
     * 加载最近的快照事件
     *
     * @param aggregateType The type of the aggregate
     * @param identifier    The identifier of the aggregate to load the snapshot for
     * @return the serialized representation of the last known snapshot event
     */
	SerializedAggregateEventData<T> loadLastSnapshotEvent(String aggregateType, Identifier<?> identifier);

    /**
     * 获取指定聚合的聚合事件集合，根据 sequenceNumber 和 batchSize获取对应的聚合事件
     *
     * @param aggregateType       The type identifier of the aggregate that generated the event
     * @param identifier          The identifier of the aggregate to load the snapshot for
     * @param firstSequenceNumber The sequence number of the first event to include in the batch
     * @param batchSize           The number of entries to include in the batch (if available)
     * @return a List of serialized representations of Events included in this batch
     */
    Iterator<? extends SerializedAggregateEventData<T>> fetchAggregateStream(String aggregateType, Identifier<?> identifier,
                                                                          long firstSequenceNumber, int batchSize);

    /**
     * 根据 条件语句 找出 需要的序列化聚合事件
     * 
     * @param whereClause The sql for query
     * @param parameters  A map containing all the parameter values for parameter keys included in the where clause
     * @param batchSize   The total number of events to return in this batch
     * @return a List of serialized representations of Events included in this batch
     */
    Iterator<? extends SerializedAggregateEventData<T>> fetchFiltered(String whereClause, List<Object> parameters,
                                                                   int batchSize);

    /**
     * 移除旧的快照事件，根据 maxSnapshotsArchived Number决定 保留多个 快照事件在移除后
     *
     * @param type                    the type of the aggregate 
     * @param mostRecentSnapshotEvent the last appended snapshot event
     * @param maxSnapshotsArchived    the number of snapshots that may remain archived
     */
    void pruneSnapshots(String type, AggregateEvent mostRecentSnapshotEvent, int maxSnapshotsArchived);

    /**
     * 持久化被序列化的事件
     *
     * @param aggregateType      The type of the aggregate
     * @param snapshotEvent      The actual snapshot event instance.
     * @param serializedPayload  The serialized payload of the event
     */
    void persistSnapshot(String aggregateType, AggregateEvent snapshotEvent, SerializedObject<T> serializedPayload);

    /**
     * 返回被存储的序列化事件的类型
     *
     * @return the type used to store serialized payloads
     */
    Class<T> getDataType();
}
