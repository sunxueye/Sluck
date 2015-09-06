package org.sluckframework.cqrs.eventsourcing;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.aggregate.GenericAggregateEvent;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 聚合快照 创建者 的具体实现，聚合快照事件 其实就是 存储当前聚合的状态
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:26:55	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class AggregateSnapshotter extends AbstractSnapshotter {

    private final Map<String, AggregateFactory<?>> aggregateFactories = new ConcurrentHashMap<String, AggregateFactory<?>>();

	@SuppressWarnings("unchecked")
	@Override
    protected AggregateEvent createSnapshot(String typeIdentifier, Identifier<?> aggregateIdentifier,
    		AggregateEventStream eventStream) {

        AggregateEvent firstEvent = eventStream.peek();
        AggregateFactory<?> aggregateFactory = aggregateFactories.get(typeIdentifier);
        EventSourcedAggregateRoot aggregate = aggregateFactory.createAggregate(aggregateIdentifier, firstEvent);
        aggregate.initializeState(eventStream);

        return new GenericAggregateEvent(
                aggregate.getIdentifier(), aggregate.getVersion(), aggregate);
    }

    /**
     * 设置聚合工厂
     *
     * @param aggregateFactories The list of aggregate factories creating the aggregates to store. May not be
     *                           <code>null</code> or contain any <code>null</code> values.
     */
    public void setAggregateFactories(List<AggregateFactory<?>> aggregateFactories) {
        for (AggregateFactory<?> factory : aggregateFactories) {
            this.aggregateFactories.put(factory.getTypeIdentifier(), factory);
        }
    }
}
