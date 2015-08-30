package org.sluckframework.domain.event.eventstore;

import org.sluckframework.domain.event.aggregate.AggregateEvent;


/**
 * 对接受到的聚合事件进行处理
 * 
 * @author sunxy
 * @time 2015年8月29日 下午5:25:15
 * @since 1.0
 */
public interface EventVisitor {
	
	 /**
     * Called for each event loaded by the event store.
     *
     * @param domainEvent The loaded event
     */
    @SuppressWarnings("rawtypes")
	void doWithEvent(AggregateEvent domainEvent);

}
