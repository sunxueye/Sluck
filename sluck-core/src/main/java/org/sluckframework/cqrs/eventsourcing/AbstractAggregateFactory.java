package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.cqrs.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 聚合工厂的抽象基类
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:57:49	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractAggregateFactory<T extends EventSourcedAggregateRoot> implements AggregateFactory<T> {

    private final ParameterResolverFactory parameterResolverFactory;

    protected AbstractAggregateFactory() {
        this(null);
    }

    protected AbstractAggregateFactory(ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T createAggregate(Identifier<?> aggregateIdentifier, AggregateEvent firstEvent) {
        T aggregate;
        if (EventSourcedAggregateRoot.class.isAssignableFrom(firstEvent.getPayloadType())) {
            aggregate = (T) firstEvent.getPayload();
        } else {
            aggregate = doCreateAggregate(aggregateIdentifier, firstEvent);
        }
        if (parameterResolverFactory != null && aggregate instanceof AbstractAnnotatedAggregateRoot) {
            ((AbstractAnnotatedAggregateRoot) aggregate).registerParameterResolverFactory(parameterResolverFactory);
        }
        return postProcessInstance(aggregate);
    }

    /**
     * 提供重写使用
     *
     * @param aggregate The aggregate to post-process.
     * @return The aggregate to initialize with the Event Stream
     */
    protected T postProcessInstance(T aggregate) {
        return aggregate;
    }

    /**
     * 根据指定的信息创建聚合
     *
     * @param aggregateIdentifier The identifier of the aggregate to create
     * @param firstEvent          The first event in the Event Stream of the Aggregate
     * @return The aggregate instance to initialize with the Event Stream
     */
    protected abstract T doCreateAggregate(Identifier<?> aggregateIdentifier, AggregateEvent firstEvent);

}
