package org.sluckframework.cqrs.saga.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.annotation.AbstractAnnotatedHandlerDefinition;
import org.sluckframework.common.annotation.MethodMessageHandler;
import org.sluckframework.common.annotation.MethodMessageHandlerInspector;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.domain.event.EventProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 从注解的saga解析出事件处理器的配置信息
 *
 * Author: sunxy
 * Created: 2015-09-14 18:26
 * Since: 1.0
 */
public class SagaMethodMessageHandlerInspector<T extends AbstractAnnotatedSaga> {

    private static final Logger logger = LoggerFactory.getLogger(SagaMethodMessageHandlerInspector.class);

    private static final ConcurrentMap<Class<?>, SagaMethodMessageHandlerInspector> INSPECTORS = new ConcurrentHashMap<Class<?>, SagaMethodMessageHandlerInspector>();

    private final Set<SagaMethodMessageHandler> handlers = new TreeSet<>();
    private final Class<T> sagaType;
    private final ParameterResolverFactory parameterResolverFactory;

    /**
     * 返回处理指定 saga 的事件方法提取器
     *
     * @param sagaType                 The type of Saga to get the inspector for
     * @param parameterResolverFactory The factory for parameter resolvers that resolve parameters for the annotated
     *                                 methods
     * @param <T>                      The type of Saga to get the inspector for
     * @return The inspector for the given saga type
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractAnnotatedSaga> SagaMethodMessageHandlerInspector<T> getInstance(
            Class<T> sagaType, ParameterResolverFactory parameterResolverFactory) {
        SagaMethodMessageHandlerInspector<T> sagaInspector = INSPECTORS.get(sagaType);
        if (sagaInspector == null || sagaInspector.getParameterResolverFactory() != parameterResolverFactory) {
            sagaInspector = new SagaMethodMessageHandlerInspector<T>(sagaType, parameterResolverFactory);

            INSPECTORS.put(sagaType, sagaInspector);
        }
        return sagaInspector;
    }

    /**
     * 使用指定的 sagaType 和 parameterResolverFactory 初始化
     *
     * @param sagaType                 The type of saga this inspector handles
     * @param parameterResolverFactory The factory for parameter resolvers that resolve parameters for the annotated
     *                                 methods
     */
    protected SagaMethodMessageHandlerInspector(Class<T> sagaType, ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
        MethodMessageHandlerInspector inspector = MethodMessageHandlerInspector.getInstance(
                sagaType, parameterResolverFactory, true,
                AnnotatedHandlerDefinition.INSTANCE);
        for (MethodMessageHandler handler : inspector.getHandlers()) {
            handlers.add(SagaMethodMessageHandler.getInstance(handler));
        }
        this.sagaType = sagaType;
    }

    /**
     * 返回给定事件的对应的 saga 处理方法
     *
     * @param event The Event to investigate the handler for
     * @return the configuration of the handlers, as defined by the annotations.
     */
    public List<SagaMethodMessageHandler> getMessageHandlers(EventProxy<?> event) {
        List<SagaMethodMessageHandler> found = new ArrayList<>(1);
        for (SagaMethodMessageHandler handler : handlers) {
            if (handler.matches(event)) {
                found.add(handler);
            }
        }
        return found;
    }

    /**
     * 从指定的注解 saga 中找出对应的 evetHandler
     *
     * @param target The instance to find a handler method on
     * @param event  The event to find a handler for
     * @return the most suitable handler for the event on the target, or an instance describing no such handler exists
     */
    public SagaMethodMessageHandler findHandlerMethod(AbstractAnnotatedSaga target, EventProxy<?> event) {
        for (SagaMethodMessageHandler handler : getMessageHandlers(event)) {
            final AssociationValue associationValue = handler.getAssociationValue(event);
            if (target.getAssociationValues().contains(associationValue)) {
                return handler;
            } else if (logger.isDebugEnabled()) {
                logger.debug(
                        "Skipping handler [{}], it requires an association value [{}:{}] that this Saga is not associated with",
                        handler.getName(),
                        associationValue.getKey(),
                        associationValue.getValue());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("No suitable handler was found for event of type", event.getPayloadType().getName());
        }
        return SagaMethodMessageHandler.noHandler();
    }

    /**
     * 返回处理的 saga 类型
     *
     * @return the type of saga (Class) this inspector handles
     */
    @SuppressWarnings({"unchecked"})
    public Class<T> getSagaType() {
        return sagaType;
    }

    /**
     * 返回参数提取工厂
     *
     * @return the ParameterResolverFactory used by this inspector
     */
    public ParameterResolverFactory getParameterResolverFactory() {
        return parameterResolverFactory;
    }

    private static final class AnnotatedHandlerDefinition extends
            AbstractAnnotatedHandlerDefinition<SagaEventHandler> {

        private static final AnnotatedHandlerDefinition INSTANCE = new AnnotatedHandlerDefinition();

        private AnnotatedHandlerDefinition() {
            super(SagaEventHandler.class);
        }

        @Override
        protected Class<?> getDefinedPayload(SagaEventHandler annotation) {
            return annotation.payloadType();
        }
    }
}