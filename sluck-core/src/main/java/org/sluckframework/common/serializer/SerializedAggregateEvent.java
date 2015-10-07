package org.sluckframework.common.serializer;

import org.joda.time.DateTime;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.aggregate.GenericAggregateEvent;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 序列话的 聚合事件信息
 * 
 * @author sunxy
 * @time 2015年8月31日 上午12:35:58
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class SerializedAggregateEvent <T> implements AggregateEvent, SerializationAware{

	private static final long serialVersionUID = 8109101200836461946L;
	
    private final long sequenceNumber;
    
    private final Identifier<?> aggregateIdentifier;
    private final SerializedEventProxy<T> eventMessage;

    public SerializedAggregateEvent(SerializedAggregateEventData domainEventData, Serializer serializer) {
        eventMessage = new SerializedEventProxy<>(
                domainEventData.getEventIdentifier(), domainEventData.getTimestamp(),
                domainEventData.getPayload(), serializer);
        aggregateIdentifier = domainEventData.getAggregateIdentifier();
        sequenceNumber = domainEventData.getSequenceNumber();
    }

    /**
     * 使用 序列化 事件 和 聚合标识 和 事件 sequence 初始化
     *
     * @param eventMessage        The eventMessage to wrap
     * @param aggregateIdentifier The identifier of the aggregate that generated the message
     * @param sequenceNumber      The sequence number of the generated event
     */
    public SerializedAggregateEvent(SerializedEventProxy<T> eventMessage, Identifier<?> aggregateIdentifier,
                                        long sequenceNumber) {
        this.eventMessage = eventMessage;
        this.aggregateIdentifier = aggregateIdentifier;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public <R> SerializedObject<R> serializePayload(Serializer serializer, Class<R> expectedRepresentation) {
        return eventMessage.serializePayload(serializer, expectedRepresentation);
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public Identifier<?> getAggregateIdentifier() {
        return aggregateIdentifier;
    }

    @Override
    public Class getPayloadType() {
        return eventMessage.getPayloadType();
    }

    @Override
    public T getPayload() {
        return eventMessage.getPayload();
    }

    @Override
    public DateTime occurredOn() {
        return eventMessage.occurredOn();
    }

    @Override
    public Object getIdentifier() {
        return eventMessage.getIdentifier();
    }

    public boolean isPayloadDeserialized() {
        return eventMessage.isPayloadDeserialized();
    }

    /**
     * Java Serialization API Method that provides a replacement to serialize, as the fields contained in this instance
     * are not serializable themselves.
     *
     * @return the GenericDomainEventMessage to use as a replacement when serializing
     */
    @SuppressWarnings("unchecked")
	protected Object writeReplace() {
        return new GenericAggregateEvent(getIdentifier(), occurredOn(),
                                                getAggregateIdentifier(), getSequenceNumber(),
                                                getPayload());
    }

}
