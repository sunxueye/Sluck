package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.identifier.Identifier;


/**
 * 聚合工厂，创建聚合
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:29:01	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public interface AggregateFactory<T extends EventSourcedAggregateRoot> {

    /**
     * 根据聚合第一个事件 和 聚合的标识符 创建聚合，第一事件 也可以是 聚合快照事件
     *
     * @param aggregateIdentifier the aggregate identifier of the aggregate to instantiate
     * @param firstEvent          The first event in the event stream. This is either the event generated during
     *                            creation of the aggregate, or a snapshot event
     * @return an aggregate ready for initialization using a DomainEventStream.
     */
    T createAggregate(Identifier<?> aggregateIdentifier, AggregateEvent firstEvent);

    /**
     * 返回 标识符 的类型
     *
     * @return the type identifier of the aggregates this repository stores
     */
    String getTypeIdentifier();

    /**
     * 返回聚合类型
     *
     * @return The type of aggregate created by this factory
     */
    Class<T> getAggregateType();

}
