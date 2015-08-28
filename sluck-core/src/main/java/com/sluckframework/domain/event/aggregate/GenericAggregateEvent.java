package com.sluckframework.domain.event.aggregate;

import com.sluckframework.domain.identifier.Identifier;

/**
 * 通用的聚合事件, 为了给没有聚合ID的 事件重建
 * 
 * @author sunxy
 * @time 2015年8月28日 下午4:57:53	
 * @since 1.0
 */
public class GenericAggregateEvent<T, ID extends Identifier<?>> extends AbstracttAggregateEvent<T, ID> {

	private static final long serialVersionUID = 8583349603712770731L;
	
	@SuppressWarnings("rawtypes")
	private transient AggregateEvent originalEvent;
	
	@SuppressWarnings("rawtypes")
	public GenericAggregateEvent(AggregateEvent originalEvent, ID aggregateId) {
		super((String)originalEvent.getIdentifier(), originalEvent.occurredOn(), 
				originalEvent.getSequenceNumber(), aggregateId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getPayloadType() {
		return originalEvent.getPayloadType();
	}

}
