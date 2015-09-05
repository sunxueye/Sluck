package org.sluckframework.cqrs.eventsourcing;

import java.util.Collection;

import org.sluckframework.common.exception.Assert;
import org.sluckframework.domain.event.aggregate.AggregateEvent;

/**
 * 聚合根实体的 抽象 实现
 * 
 * @author sunxy
 * @time 2015年9月5日 下午11:56:10
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractEventSourcedEntity implements EventSourcedEntity {

	private volatile AbstractEventSourcedAggregateRoot aggregateRoot;

    @Override
    public void registerAggregateRoot(AbstractEventSourcedAggregateRoot aggregateRootToRegister) {
        if (this.aggregateRoot != null && this.aggregateRoot != aggregateRootToRegister) {
            throw new IllegalStateException("Cannot register new aggregate. "
                                                    + "This entity is already part of another aggregate");
        }
        this.aggregateRoot = aggregateRootToRegister;
    }

    @Override
    public void handleRecursively(AggregateEvent event) {
        handle(event);
        Collection<? extends EventSourcedEntity> childEntities = getChildEntities();
        if (childEntities != null) {
            for (EventSourcedEntity entity : childEntities) {
                if (entity != null) {
                    entity.registerAggregateRoot(aggregateRoot);
                    entity.handleRecursively(event);
                }
            }
        }
    }

    protected abstract Collection<? extends EventSourcedEntity> getChildEntities();

    protected abstract void handle(AggregateEvent event);

    /**
     * 处理事件， 聚合实体 处理 所有来自聚合的未提交事件
     *
     * @param event The payload of the event to apply
     */
    protected void apply(Object event) {
    	 Assert.notNull(aggregateRoot, "The aggregate root is unknown. "
                 + "Is this entity properly registered as the child of an aggregate member?");
         aggregateRoot.apply(event);
    }

    /**
     * Returns ar
     *
     * @return the reference to the root of the aggregate this entity is a member of
     */
    protected AbstractEventSourcedAggregateRoot getAggregateRoot() {
        return aggregateRoot;
    }
}
