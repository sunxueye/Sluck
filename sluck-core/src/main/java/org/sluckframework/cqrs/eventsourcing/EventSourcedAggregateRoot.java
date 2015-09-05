package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.identifier.Identifier;

/**
 * es 聚合根
 * 
 * @author sunxy
 * @time 2015年9月5日 下午11:13:43
 * @since 1.0
 */
public interface EventSourcedAggregateRoot<ID extends Identifier<?>> extends AggregateRoot<ID >{

    /**
     * 根据聚合事件流初始化状态
     *
     * @param domainEventStream   the event stream containing the events that describe the state changes of this
     *                            aggregate
     */
    void initializeState(AggregateEventStream domainEventStream);

}
