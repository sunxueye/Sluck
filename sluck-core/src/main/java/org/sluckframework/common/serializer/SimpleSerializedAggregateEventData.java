package org.sluckframework.common.serializer;

import org.joda.time.DateTime;
import org.sluckframework.domain.identifier.Identifier;

/**
 * SerializedAggregateEventData 简单实现
 * 
 * @author sunxy
 * @time 2015年8月30日 下午11:22:54
 * @since 1.0
 */
public class SimpleSerializedAggregateEventData<T> implements SerializedAggregateEventData<T> {

    private final String eventIdentifier;
    private final Identifier<?> aggregateIdentifier;
    private final long sequenceNumber;
    private final DateTime timestamp;
    private final SerializedObject<T> serializedPayload;

    @SuppressWarnings("unchecked")
    public SimpleSerializedAggregateEventData(String eventIdentifier, Identifier<?> aggregateIdentifier, // NOSONAR - Long ctor
                                           long sequenceNumber, Object timestamp, String payloadType,
                                           String payloadRevision, T payload) { // NOSONAR
        this(eventIdentifier, aggregateIdentifier, sequenceNumber, timestamp,
             new SimpleSerializedObject<T>(payload, (Class<T>) payload.getClass(),
                                           payloadType, payloadRevision));
    }

    public SimpleSerializedAggregateEventData(String eventIdentifier, Identifier<?> aggregateIdentifier, // NOSONAR - Long ctor
                                           long sequenceNumber, Object timestamp,
                                           SerializedObject<T> serializedPayload) {
        this.eventIdentifier = eventIdentifier;
        this.aggregateIdentifier = aggregateIdentifier;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = new DateTime(timestamp);
        this.serializedPayload = serializedPayload;
    }

    @Override
    public String getEventIdentifier() {
        return eventIdentifier;
    }

    @Override
    public Identifier<?> getAggregateIdentifier() {
        return aggregateIdentifier;
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public DateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public SerializedObject<T> getPayload() {
        return serializedPayload;
    }
}
