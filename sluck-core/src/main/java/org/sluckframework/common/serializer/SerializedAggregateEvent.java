package org.sluckframework.common.serializer;

import java.util.Map;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.GenericDomainEventMessage;
import org.axonframework.domain.MetaData;
import org.axonframework.serializer.SerializedDomainEventData;
import org.axonframework.serializer.SerializedDomainEventMessage;
import org.axonframework.serializer.SerializedEventMessage;
import org.axonframework.serializer.SerializedObject;
import org.axonframework.serializer.Serializer;
import org.joda.time.DateTime;
import org.sluckframework.domain.event.aggregate.AggregateEvent;


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
    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private final Object aggregateIdentifier;
    private final SerializedEventProxy<T> eventMessage;

    public SerializedDomainEventMessage(SerializedDomainEventData domainEventData, Serializer serializer) {
        eventMessage = new SerializedEventMessage<T>(
                domainEventData.getEventIdentifier(), domainEventData.getTimestamp(),
                domainEventData.getPayload(), domainEventData.getMetaData(), serializer);
        aggregateIdentifier = domainEventData.getAggregateIdentifier();
        sequenceNumber = domainEventData.getSequenceNumber();
    }

    /**
     * Wrapper constructor for wrapping a SerializedEventMessage as a SerializedDomainEventMessage, using given
     * <code>aggregateIdentifier</code> and <code>sequenceNumber</code>. This constructor should be used to reconstruct
     * an instance of an existing serialized Domain Event Message
     *
     * @param eventMessage        The eventMessage to wrap
     * @param aggregateIdentifier The identifier of the aggregate that generated the message
     * @param sequenceNumber      The sequence number of the generated event
     */
    public SerializedDomainEventMessage(SerializedEventMessage<T> eventMessage, Object aggregateIdentifier,
                                        long sequenceNumber) {
        this.eventMessage = eventMessage;
        this.aggregateIdentifier = aggregateIdentifier;
        this.sequenceNumber = sequenceNumber;
    }

    private SerializedDomainEventMessage(SerializedDomainEventMessage<T> original, Map<String, ?> metaData) {
        eventMessage = original.eventMessage.withMetaData(metaData);
        this.aggregateIdentifier = original.getAggregateIdentifier();
        this.sequenceNumber = original.getSequenceNumber();
    }

    @Override
    public <R> SerializedObject<R> serializePayload(Serializer serializer, Class<R> expectedRepresentation) {
        return eventMessage.serializePayload(serializer, expectedRepresentation);
    }

    @Override
    public <R> SerializedObject<R> serializeMetaData(Serializer serializer, Class<R> expectedRepresentation) {
        return eventMessage.serializeMetaData(serializer, expectedRepresentation);
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public Object getAggregateIdentifier() {
        return aggregateIdentifier;
    }

    @Override
    public DomainEventMessage<T> withMetaData(Map<String, ?> newMetaData) {
        if (eventMessage.isPayloadDeserialized()) {
            return new GenericDomainEventMessage<T>(getIdentifier(), getTimestamp(),
                                                    aggregateIdentifier, sequenceNumber,
                                                    getPayload(), newMetaData);
        } else {
            return new SerializedDomainEventMessage<T>(this, newMetaData);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This method will force the MetaData to be deserialized if not already done.
     */
    @Override
    public DomainEventMessage<T> andMetaData(Map<String, ?> additionalMetaData) {
        MetaData newMetaData = getMetaData().mergedWith(additionalMetaData);
        return withMetaData(newMetaData);
    }

    @Override
    public Class getPayloadType() {
        return eventMessage.getPayloadType();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public T getPayload() {
        return eventMessage.getPayload();
    }

    @Override
    public MetaData getMetaData() {
        return eventMessage.getMetaData();
    }

    @Override
    public DateTime getTimestamp() {
        return eventMessage.getTimestamp();
    }

    @Override
    public String getIdentifier() {
        return eventMessage.getIdentifier();
    }

    /**
     * Indicates whether the payload of this message has already been deserialized.
     *
     * @return <code>true</code> if the payload is deserialized, otherwise <code>false</code>
     */
    public boolean isPayloadDeserialized() {
        return eventMessage.isPayloadDeserialized();
    }

    /**
     * Java Serialization API Method that provides a replacement to serialize, as the fields contained in this instance
     * are not serializable themselves.
     *
     * @return the GenericDomainEventMessage to use as a replacement when serializing
     */
    protected Object writeReplace() {
        return new GenericDomainEventMessage<T>(getIdentifier(), getTimestamp(),
                                                getAggregateIdentifier(), getSequenceNumber(),
                                                getPayload(), getMetaData());
    }

}
