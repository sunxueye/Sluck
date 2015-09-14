package org.sluckframework.cqrs.saga.annotation;

import org.sluckframework.common.annotation.ClasspathParameterResolverFactory;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.cqrs.saga.*;
import org.sluckframework.domain.event.EventProxy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于注解的 sagaManager
 *
 * Author: sunxy
 * Created: 2015-09-14 21:33
 * Since: 1.0
 */
public class AnnotatedSagaManager extends AbstractSagaManager {

    private final ParameterResolverFactory parameterResolverFactory;


    /**
     * 使用给定的 rpt 和 sagaClass 初始化
     *
     * @param sagaRepository The repository providing access to the Saga instances
     * @param sagaClasses    The types of Saga that this instance should manage
     */
    public AnnotatedSagaManager(SagaRepository sagaRepository,
                                Class<? extends AbstractAnnotatedSaga>... sagaClasses) {
        this(sagaRepository,
                ClasspathParameterResolverFactory.forClass(sagaClasses.length > 0 ? sagaClasses[0] : sagaRepository.getClass()),
                sagaClasses);
    }

    /**
     * 使用给定的 rpt 和 sagaClass 和 参数解析工厂初始化
     *
     * @param sagaRepository           The repository providing access to the Saga instances
     * @param parameterResolverFactory The parameterResolverFactory to resolve parameters with for the saga instance's
     *                                 handler methods
     * @param sagaClasses              The types of Saga that this instance should manage
     */
    public AnnotatedSagaManager(SagaRepository sagaRepository, ParameterResolverFactory parameterResolverFactory,
                                Class<? extends AbstractAnnotatedSaga>... sagaClasses) {
        this(sagaRepository, new GenericSagaFactory(), parameterResolverFactory, sagaClasses);
    }

    /**
     * 使用给定的资源初始化
     *
     * @param sagaRepository The repository providing access to the Saga instances
     * @param sagaFactory    The factory creating new instances of a Saga
     * @param sagaClasses    The types of Saga that this instance should manage
     */
    public AnnotatedSagaManager(SagaRepository sagaRepository, SagaFactory sagaFactory,
                                Class<? extends AbstractAnnotatedSaga>... sagaClasses) {
        this(sagaRepository, sagaFactory,
                ClasspathParameterResolverFactory.forClass(sagaClasses.length > 0 ? sagaClasses[0] : sagaRepository.getClass()),
                sagaClasses);
    }

    /**
     * 使用给定的资源初始化
     *
     * @param sagaRepository           The repository providing access to the Saga instances
     * @param sagaFactory              The factory creating new instances of a Saga
     * @param parameterResolverFactory The parameterResolverFactory to resolve parameters with for the saga instance's
     *                                 handler methods
     * @param sagaClasses              The types of Saga that this instance should manage
     */
    public AnnotatedSagaManager(SagaRepository sagaRepository, SagaFactory sagaFactory,
                                ParameterResolverFactory parameterResolverFactory,
                                Class<? extends AbstractAnnotatedSaga>... sagaClasses) {
        super(sagaRepository, sagaFactory, sagaClasses);
        this.parameterResolverFactory = parameterResolverFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SagaInitializationPolicy getSagaCreationPolicy(Class<? extends Saga> sagaType, EventProxy<?> event) {
        SagaMethodMessageHandlerInspector<? extends AbstractAnnotatedSaga> inspector =
                SagaMethodMessageHandlerInspector.getInstance((Class<? extends AbstractAnnotatedSaga>) sagaType,
                        parameterResolverFactory);
        final List<SagaMethodMessageHandler> handlers = inspector.getMessageHandlers(event);
        for (SagaMethodMessageHandler handler : handlers) {
            if (handler.getCreationPolicy() != SagaCreationPolicy.NONE) {
                return new SagaInitializationPolicy(handler.getCreationPolicy(), handler.getAssociationValue(event));
            }
        }
        return SagaInitializationPolicy.NONE;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<AssociationValue> extractAssociationValues(Class<? extends Saga> sagaType,
                                                             EventProxy<?> event) {
        SagaMethodMessageHandlerInspector<? extends AbstractAnnotatedSaga> inspector =
                SagaMethodMessageHandlerInspector.getInstance((Class<? extends AbstractAnnotatedSaga>) sagaType,
                        parameterResolverFactory);
        final List<SagaMethodMessageHandler> handlers = inspector.getMessageHandlers(event);
        Set<AssociationValue> values = new HashSet<>(handlers.size());
        for (SagaMethodMessageHandler handler : handlers) {
            values.add(handler.getAssociationValue(event));
        }
        return values;
    }

    @Override
    protected void preProcessSaga(Saga saga) {
        if (parameterResolverFactory != null) {
            ((AbstractAnnotatedSaga) saga).registerParameterResolverFactory(parameterResolverFactory);
        }
    }

    @Override
    public Class<?> getTargetType() {
        if (getManagedSagaTypes().isEmpty()) {
            return Void.TYPE;
        }
        return getManagedSagaTypes().iterator().next();
    }
}
