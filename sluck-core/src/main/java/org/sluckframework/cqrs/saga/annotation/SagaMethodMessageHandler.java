package org.sluckframework.cqrs.saga.annotation;

import org.sluckframework.common.annotation.MessageHandlerInvocationException;
import org.sluckframework.common.annotation.MethodMessageHandler;
import org.sluckframework.common.exception.SluckConfigurationException;
import org.sluckframework.common.property.Property;
import org.sluckframework.common.property.PropertyAccessStrategy;
import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.SagaCreationPolicy;
import org.sluckframework.domain.event.EventProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.String.format;

/**
 * 包含处理事件的注解方法的 代理处理器
 * 
 * Author: sunxy
 * Created: 2015-09-14 17:04
 * Since: 1.0
 */
public class SagaMethodMessageHandler implements Comparable<SagaMethodMessageHandler> {

    private static final SagaMethodMessageHandler NO_HANDLER_CONFIGURATION =
            new SagaMethodMessageHandler(SagaCreationPolicy.NONE, null, null, null);

    /**
     * 返回空的事件处理
     *
     * @return a SagaMethodMessageHandler indicating that a inspected method is *not* a SagaEventHandler
     */
    public static SagaMethodMessageHandler noHandler() {
        return NO_HANDLER_CONFIGURATION;
    }

    private final SagaCreationPolicy creationPolicy;
    private final MethodMessageHandler handlerMethod;
    private final String associationKey;
    private final Property associationProperty;

    /**
     * 使用指定的事件处理方法初始化,将指定一些saga的信息,如关联值和创建策略
     *
     * @param methodHandler The handler for incoming events
     * @return a SagaMethodMessageHandler for the handler
     */
    @SuppressWarnings("unchecked")
    public static SagaMethodMessageHandler getInstance(MethodMessageHandler methodHandler) {
        Method handlerMethod = methodHandler.getMethod();
        SagaEventHandler handlerAnnotation = handlerMethod.getAnnotation(SagaEventHandler.class);
        String associationPropertyName = handlerAnnotation.associationProperty();
        Property associationProperty = PropertyAccessStrategy.getProperty(methodHandler.getPayloadType(),
                associationPropertyName);
        if (associationProperty == null) {
            throw new SluckConfigurationException(format("SagaEventHandler %s.%s defines a property %s that is not "
                            + "defined on the Event it declares to handle (%s)",
                    methodHandler.getMethod().getDeclaringClass().getName(),
                    methodHandler.getMethodName(), associationPropertyName,
                    methodHandler.getPayloadType().getName()
            ));
        }
        String associationKey = handlerAnnotation.keyName().isEmpty()
                ? associationPropertyName
                : handlerAnnotation.keyName();
        StartSaga startAnnotation = handlerMethod.getAnnotation(StartSaga.class);
        SagaCreationPolicy sagaCreationPolicy;
        if (startAnnotation == null) {
            sagaCreationPolicy = SagaCreationPolicy.NONE;
        } else if (startAnnotation.forceNew()) {
            sagaCreationPolicy = SagaCreationPolicy.ALWAYS;
        } else {
            sagaCreationPolicy = SagaCreationPolicy.IF_NONE_FOUND;
        }

        return new SagaMethodMessageHandler(sagaCreationPolicy, methodHandler, associationKey, associationProperty);
    }

    /**
     * 创建一个新的saga 方法处理器
     *
     * @param creationPolicy      The creation policy for the handlerMethod
     * @param handler             The handler for the event
     * @param associationKey      The association key configured for this handler
     * @param associationProperty The association property configured for this handler
     */
    protected SagaMethodMessageHandler(SagaCreationPolicy creationPolicy, MethodMessageHandler handler,
                                       String associationKey, Property associationProperty) {
        this.creationPolicy = creationPolicy;
        this.handlerMethod = handler;
        this.associationKey = associationKey;
        this.associationProperty = associationProperty;
    }

    /**
     * 判断是否能能够处理事件
     *
     * @return true if the saga has a handler
     */
    public boolean isHandlerAvailable() {
        return handlerMethod != null;
    }

    /**
     * 从指定的事件信息中获取关联值
     *
     * @param eventMessage The event message containing the value of the association
     * @return the AssociationValue to find the saga instance with, or <code>null</code> if none found
     */
    @SuppressWarnings("unchecked")
    public AssociationValue getAssociationValue(EventProxy<?> eventMessage) {
        if (associationProperty == null) {
            return null;
        }

        Object associationValue = associationProperty.getValue(eventMessage.getPayload());
        return associationValue == null ? null : new AssociationValue(associationKey, associationValue.toString());
    }

    /**
     * 返回saga的创建策略
     *
     * @return the creation policy of the inspected method
     */
    public SagaCreationPolicy getCreationPolicy() {
        return creationPolicy;
    }

    /**
     * 判断是否能够处理指定的事件
     *
     * @param message The message to inspect
     * @return <code>true</code> if this handler can handle the message, otherwise <code>false</code>.
     */
    public boolean matches(EventProxy<?> message) {
        return handlerMethod != null && handlerMethod.matches(message);
    }

    /**
     * 判断对应的当前方法是否为 end 方法
     *
     * @return <code>true</code> if the Saga lifecycle ends unconditionally after this call, otherwise
     * <code>false</code>
     */
    public boolean isEndingHandler() {
        return handlerMethod != null && handlerMethod.getMethod().isAnnotationPresent(EndSaga.class);
    }

    @Override
    public int compareTo(SagaMethodMessageHandler o) {
        if (this.handlerMethod == null && o.handlerMethod == null) {
            return 0;
        } else if (this.handlerMethod == null) {
            return -1;
        } else if (o.handlerMethod == null) {
            return 1;
        }
        final int handlerEquality = handlerMethod.compareTo(o.handlerMethod);
        if (handlerEquality == 0) {
            return o.handlerMethod.getMethod().getParameterTypes().length
                    - this.handlerMethod.getMethod().getParameterTypes().length;
        }
        return handlerEquality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SagaMethodMessageHandler that = (SagaMethodMessageHandler) o;

        return this.compareTo(that) != 0;
    }

    @Override
    public int hashCode() {
        return handlerMethod != null ? handlerMethod.hashCode() : 0;
    }

    /**
     * 执行目标对象的 事件 处理方法
     *
     * @param target  The instance to invoke a method on
     * @param message The message to use to resolve the parameters of the handler to invoke
     */
    public void invoke(Object target, EventProxy<?> message) {
        if (!isHandlerAvailable()) {
            return;
        }
        try {
            handlerMethod.invoke(target, message);
        } catch (IllegalAccessException e) {
            throw new MessageHandlerInvocationException("Access to the message handler method was denied.", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new MessageHandlerInvocationException("An exception occurred while invoking the handler method.", e);
        }
    }

    /**
     * 返回方法处理器的名称
     *
     * @return the name of the handler
     */
    public String getName() {
        return handlerMethod.getMethodName();
    }
}