package org.sluckframework.domain.event.aggregate;

import org.joda.time.DateTime;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 通用的聚合事件代理，承载了真正的聚合事件
 * 
 * @author sunxy
 * @time 2015年8月28日 下午4:57:53	
 * @since 1.0
 */
public class GenericAggregateEvent<T, ID extends Identifier<?>> extends GenericEvent<T> implements AggregateEvent<T, ID> {

	private static final long serialVersionUID = 8583349603712770731L;
	
    private final long sequenceNumber;
	
	private ID aggregateIdentifier;
	
	public GenericAggregateEvent(ID aggregateIdentifier, long sequenceNumber, T payload) {
		super(payload);
		this.aggregateIdentifier = aggregateIdentifier;
		this.sequenceNumber = sequenceNumber;
	}
	
	public GenericAggregateEvent(Object identifier, DateTime timestamp, 
			ID aggregateIdentifier, long sequenceNumber, T payload) {
		super(identifier, payload, timestamp);
		this.aggregateIdentifier = aggregateIdentifier;
		this.sequenceNumber = sequenceNumber;
	}
	
	private GenericAggregateEvent(AggregateEvent<T,ID> original) {
		super(original.getIdentifier(), original.getPayload(), original.occurredOn());
		this.aggregateIdentifier = original.getAggregateIdentifier();
		this.sequenceNumber = original.getSequenceNumber();
	}

	@Override
	public ID getAggregateIdentifier() {
		return aggregateIdentifier;
	}

	@Override
	public long getSequenceNumber() {
		return sequenceNumber;
	}
	
	@Override
	public String toString() {
		return  String.format("GenericAggregateEvent[%s]", getPayloadType().getName() + ", id:" + getIdentifier());
	}

}
