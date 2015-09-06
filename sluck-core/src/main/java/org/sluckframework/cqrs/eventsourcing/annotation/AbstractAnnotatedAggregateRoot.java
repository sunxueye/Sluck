package org.sluckframework.cqrs.eventsourcing.annotation;

import java.util.Collection;

import org.sluckframework.common.annotation.ClasspathParameterResolverFactory;
import org.sluckframework.common.annotation.MessageHandlerInvoker;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.cqrs.eventsourcing.AbstractEventSourcedAggregateRoot;
import org.sluckframework.cqrs.eventsourcing.EventSourcedEntity;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 基于注解的 聚合根 ， 为了最大化 减少 开发者 和 框架的耦合
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:11:05	
 * @since 1.0
 */
public abstract class AbstractAnnotatedAggregateRoot<ID extends Identifier<?>> extends AbstractEventSourcedAggregateRoot<ID> {

	private static final long serialVersionUID = 3582253633887127624L;
	
	private transient MessageHandlerInvoker eventHandlerInvoker; // NOSONAR
    private transient AggregateAnnotationInspector inspector; // NOSONAR
    private transient ParameterResolverFactory parameterResolverFactory; // NOSONAR

    /**
     * call 合适的 事件处理器 处理聚合事件
     *
     * @param event The event to handle
     */
    @SuppressWarnings("rawtypes")
	@Override
    protected void handle(AggregateEvent event) {
        ensureInspectorInitialized();
        ensureInvokerInitialized();
        eventHandlerInvoker.invokeHandlerMethod(event);
    }
    
    @Override
    public ID getIdentifier() {
        ensureInspectorInitialized();
        return inspector.getIdentifier(this);
    }

    @Override
    protected Collection<EventSourcedEntity> getChildEntities() {
        ensureInspectorInitialized();
        return inspector.getChildEntities(this);
    }

    private void ensureInvokerInitialized() {
        if (eventHandlerInvoker == null) {
            ensureInspectorInitialized();
            eventHandlerInvoker = inspector.createEventHandlerInvoker(this);
        }
    }

    @SuppressWarnings("rawtypes")
	private void ensureInspectorInitialized() {
        if (inspector == null) {
            final Class<? extends AbstractAnnotatedAggregateRoot> aggregateType = getClass();
            inspector = AggregateAnnotationInspector.getInspector(aggregateType, createParameterResolverFactory());
        }
    }

    /**
     * 创建一个 ParameterResolverFactory 用于 聚合根解析 @EventSourcingHandler
     *
     * @return the parameter resolver with which to resolve parameters for event handler methods.
     */
    protected ParameterResolverFactory createParameterResolverFactory() {
        if (parameterResolverFactory == null && CurrentUnitOfWork.isStarted()) {
            parameterResolverFactory = CurrentUnitOfWork.get().getResource(ParameterResolverFactory.class.getName());
        }
        if (parameterResolverFactory == null) {
            parameterResolverFactory = ClasspathParameterResolverFactory.forClass(getClass());
        }
        return parameterResolverFactory;
    }

    /**
     * 注册 parameterResolverFactory 解析工厂
     *
     * @param parameterResolverFactory The factory to provide resolvers for parameters of annotated event handlers
     */
    public void registerParameterResolverFactory(ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
    }

}
