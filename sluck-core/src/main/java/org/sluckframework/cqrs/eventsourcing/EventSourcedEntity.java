package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.domain.event.aggregate.AggregateEvent;

/**
 * 聚合中的实体，可能会需要处理聚合事件和发布聚合事件
 * 
 * @author sunxy
 * @time 2015年9月5日 下午11:40:23
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public interface EventSourcedEntity {

    /**
     * 注册聚合根，表明其属于指定的聚合根
     *
     * @param aggregateRootToRegister the root of the aggregate this entity is part of.
     */
	void registerAggregateRoot(AbstractEventSourcedAggregateRoot aggregateRootToRegister);

    /**
     * 处理聚合事件
     *
     * @param event The event to handle
     */
    void handleRecursively(AggregateEvent event);

}
