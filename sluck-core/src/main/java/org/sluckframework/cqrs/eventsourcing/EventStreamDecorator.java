package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.domain.event.aggregate.AggregateEventStream;


/**
 * 事件流装饰器，在聚合事件被 读/写 的时候装饰
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:10:41	
 * @since 1.0
 */
public interface EventStreamDecorator {
	
    /**
     * 读 装饰
     *
     * @param aggregateType       The type of aggregate events are being read for
     * @param aggregateIdentifier The identifier of the aggregate events are loaded for
     * @param eventStream         The eventStream containing the events to append to the event store
     * @return the decorated event stream
     */
    AggregateEventStream decorateForRead(String aggregateType, Object aggregateIdentifier,
                                      AggregateEventStream eventStream);

    /**
     * 写 装饰
     *
     * @param aggregateType The type of aggregate events are being appended for
     * @param aggregate     The aggregate for which the events are being stored
     * @param eventStream   The eventStream containing the events to append to the event store
     * @return the decorated event stream
     */
    @SuppressWarnings("rawtypes")
	AggregateEventStream decorateForAppend(String aggregateType, EventSourcedAggregateRoot aggregate,
                                        AggregateEventStream eventStream);


}
