package org.sluckframework.domain.event.aggregate;

import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.identifier.Identifier;


/**
 * 领域事件,聚合产生的领域事件
 * 
 * @author sunxy
 * @time 2015年8月28日 下午3:26:05	
 * @since 1.0
 */
public interface AggregateEvent<T, AID extends Identifier<?>> extends EventProxy<T> {
	
	/**
	 * 产生聚合事件的 聚合根标识符
	 * 
	 * @return id
	 */
	AID getAggregateIdentifier();
	
	/**
	 * 由相同聚合产生的领域事件的事件序列号
	 * 
	 * @return sequence
	 */
	long getSequenceNumber();
}
