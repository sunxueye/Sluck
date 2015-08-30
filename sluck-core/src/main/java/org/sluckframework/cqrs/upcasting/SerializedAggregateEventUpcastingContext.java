package org.sluckframework.cqrs.upcasting;

import org.joda.time.DateTime;
import org.sluckframework.common.serializer.SerializedAggregateEventData;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.domain.identifier.Identifier;

/**
 * @author sunxy
 * @time 2015年8月31日 上午12:28:49
 * @since 1.0
 */
public class SerializedAggregateEventUpcastingContext implements UpcastingContext {

    private final Object eventIdentifier;
    private final Identifier<?> aggregateIdentifier;
    private final Long sequenceNumber;
    private final DateTime timestamp;

    @SuppressWarnings("rawtypes")
	public SerializedAggregateEventUpcastingContext(SerializedAggregateEventData domainEventData, Serializer serializer) {
        this.eventIdentifier = domainEventData.getEventIdentifier();
        this.aggregateIdentifier = domainEventData.getAggregateIdentifier();
        this.sequenceNumber = domainEventData.getSequenceNumber();
        this.timestamp = domainEventData.getTimestamp();
    }

    @Override
    public Object getEventIdentifier() {
        return eventIdentifier;
    }

    @Override
    public Identifier<?> getAggregateIdentifier() {
        return aggregateIdentifier;
    }

    @Override
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public DateTime getTimestamp() {
        return timestamp;
    }

}
