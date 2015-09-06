package org.sluckframework.common.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sluckframework.common.annotation.MessageHandlerInvocationException;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.domain.event.EventProxy;

/**
 * 支持 注解话的 事件处理
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:13:13	
 * @since 1.0
 */
public class MessageHandlerInvoker {

    private final Object target;
    private final MethodMessageHandlerInspector inspector;

    /**
     * 使用给定的 参数 初始化
     *
     * @param target                   The target to invoke methods on
     * @param parameterResolverFactory The factory to create ParameterResolvers with
     * @param allowDuplicates          Whether or not to accept multiple message handlers with the same payload type
     * @param handlerDefinition        The definition indicating which methods are message handlers
     */
    public MessageHandlerInvoker(Object target, ParameterResolverFactory parameterResolverFactory,
                                 boolean allowDuplicates, HandlerDefinition<? super Method> handlerDefinition) {
        this.inspector = MethodMessageHandlerInspector.getInstance(target.getClass(), parameterResolverFactory,
                                                                   allowDuplicates, handlerDefinition);
        this.target = target;
    }

    public Object invokeHandlerMethod(EventProxy<?> parameter) {
        MethodMessageHandler m = findHandlerMethod(parameter);
        if (m == null) {
            // event listener doesn't support this type of event
            return null;
        }
        try {
            return m.invoke(target, parameter);
        } catch (IllegalAccessException e) {
            throw new MessageHandlerInvocationException("Access to the message handler method was denied.", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new MessageHandlerInvocationException("An exception occurred while invoking the handler method.", e);
        }
    }

    public MethodMessageHandler findHandlerMethod(EventProxy<?> message) {
        return inspector.findHandlerMethod(message);
    }

    public Class<?> getTargetType() {
        return inspector.getTargetType();
    }

}
