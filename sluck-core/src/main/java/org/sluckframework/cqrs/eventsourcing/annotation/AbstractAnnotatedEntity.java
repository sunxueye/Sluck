package org.sluckframework.cqrs.eventsourcing.annotation;

import org.sluckframework.common.annotation.MessageHandlerInvoker;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.cqrs.eventsourcing.AbstractEventSourcedEntity;
import org.sluckframework.cqrs.eventsourcing.EventSourcedEntity;
import org.sluckframework.domain.event.aggregate.AggregateEvent;

import java.util.Collection;

/**
 * 支持注解的聚合中的实体,可以定义 eventHandler 处理, 在注解的聚合中,需要为实体加上
 * {@link EventSourcedMember}
 *
 * Author: sunxy
 * Created: 2015-09-10 10:15
 * Since: 1.0
 */
public abstract class AbstractAnnotatedEntity extends AbstractEventSourcedEntity{

    private transient AggregateAnnotationInspector inspector;
    private transient MessageHandlerInvoker eventHandlerInvoker;

    public AbstractAnnotatedEntity() {
    }

    /**
     * 处理 提供的事件
     *
     * @param event The event to handle
     */
    @Override
    protected void handle(AggregateEvent event) {
        // some deserialization mechanisms don't use the default constructor to initialize a class.
        ensureInspectorInitialized();
        ensureInvokerInitialized();
        eventHandlerInvoker.invokeHandlerMethod(event);
    }

    @Override
    protected Collection<EventSourcedEntity> getChildEntities() {
        ensureInspectorInitialized();
        return inspector.getChildEntities(this);
    }

    private void ensureInvokerInitialized() {
        if (eventHandlerInvoker == null) {
            eventHandlerInvoker = inspector.createEventHandlerInvoker(this);
        }
    }

    private void ensureInspectorInitialized() {
        if (inspector == null) {
            final ParameterResolverFactory parameterResolverFactory = createParameterResolverFactory();
            inspector = AggregateAnnotationInspector.getInspector(getClass(), parameterResolverFactory);
        }
    }

    /**
     * 返回或创建聚合使用的参数解析器工厂
     *
     * @return the parameter resolver with which to resolve parameters for event handler methods.
     */
    protected ParameterResolverFactory createParameterResolverFactory() {
        return ((AbstractAnnotatedAggregateRoot) getAggregateRoot()).createParameterResolverFactory();
    }
}
