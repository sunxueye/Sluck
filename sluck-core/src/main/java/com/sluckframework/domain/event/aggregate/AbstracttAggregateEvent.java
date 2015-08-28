package com.sluckframework.domain.event.aggregate;

import java.util.UUID;

import org.joda.time.DateTime;

import com.sluckframework.domain.identifier.Identifier;

/**
 * 抽象聚合事件基类, 使用String类型为标识符
 * 
 * @author sunxy
 * @time 2015年8月28日 下午3:35:07	
 * @since 1.0
 */
public abstract class AbstracttAggregateEvent<T, ID extends Identifier<?>> implements AggregateEvent<T, String, ID> {

	private static final long serialVersionUID = -4569463762678800497L;
	
	private final String identifier;
	private final DateTime timestamp;
	private final long sequenceNumber;
	
	private ID aggregateIdentifier;
	
	public AbstracttAggregateEvent(long sequenceNumber) {
		this(UUID.randomUUID().toString(), new DateTime(), sequenceNumber);
	}
	
	public AbstracttAggregateEvent(ID aggregateId, long sequenceNumber) {
		this(sequenceNumber);
		aggregateIdentifier = aggregateId;
	}
	
	public AbstracttAggregateEvent(String identifier, DateTime timestamp, long sequenceNumber) {
		this.identifier = identifier;
		this.timestamp = timestamp;
		this.sequenceNumber = sequenceNumber;
	}
	
	public AbstracttAggregateEvent(String identifier, DateTime timestamp,
			long sequenceNumber, ID aggregateIdentifier) {
		this.identifier = identifier;
		this.timestamp = timestamp;
		this.sequenceNumber = sequenceNumber;
		this.aggregateIdentifier = aggregateIdentifier;
	}
	
	protected void setAggregateIdentifier(ID aggregateIdentifier) {
		this.aggregateIdentifier = aggregateIdentifier;
	}

	@Override
	public ID getAggregateIdentifier() {
		return aggregateIdentifier;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public DateTime occurredOn() {
		return timestamp;
	}

	@Override
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		AbstracttAggregateEvent other = (AbstracttAggregateEvent) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return  String.format("AggregateEvent[%s]", getPayloadType().getName() + ", id:" + identifier);
	}

}
