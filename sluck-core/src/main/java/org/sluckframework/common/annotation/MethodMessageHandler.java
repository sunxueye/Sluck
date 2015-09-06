package org.sluckframework.common.annotation;

import static java.lang.String.format;
import static org.sluckframework.common.util.ReflectionUtils.ensureAccessible;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.ParameterResolver;

/**
 * 方法执行器
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:21:26	
 * @since 1.0
 */
public class MethodMessageHandler extends AbstractMessageHandler {

    private final Method method;

    /**
     * 为指定的 method 创建 MethodMessageHandler,使用匹配的 类型
     *
     * @param method                   The method to create a Handler for
     * @param explicitPayloadType      The payload type explicitly defined on the method, or <code>null</code>
     * @param parameterResolverFactory The strategy for resolving parameter values of handler methods
     * @return The MethodMessageHandler implementation for the given method.
     */
    @SuppressWarnings("rawtypes")
	public static MethodMessageHandler createFor(Method method, Class<?> explicitPayloadType,
                                                 ParameterResolverFactory parameterResolverFactory) {
        ParameterResolver[] resolvers = findResolvers(
                parameterResolverFactory,
                                                      method.getAnnotations(),
                                                      method.getParameterTypes(),
                                                      method.getParameterAnnotations(),
                                                      explicitPayloadType == null);
        Class<?> payloadType = explicitPayloadType;
        if (explicitPayloadType == null) {
            Class<?> firstParameter = method.getParameterTypes()[0];
            if (EventProxy.class.isAssignableFrom(firstParameter)) {
                payloadType = Object.class;
            } else {
                payloadType = firstParameter;
            }
        }
        ensureAccessible(method);
        validate(method, resolvers);
        return new MethodMessageHandler(method, resolvers, payloadType);
    }

    @Override
    public Object invoke(Object target, EventProxy<?> message) throws InvocationTargetException, IllegalAccessException {
        Assert.isTrue(method.getDeclaringClass().isInstance(target),
                      "Given target is not an instance of the method's owner.");
        Assert.notNull(message, "Event may not be null");
        Object[] parameterValues = new Object[getParameterValueResolvers().length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = getParameterValueResolvers()[i].resolveParameterValue(message);
        }
        return method.invoke(target, parameterValues);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return method.getAnnotation(annotationType);
    }

    @SuppressWarnings("rawtypes")
	private static void validate(Method method, ParameterResolver[] parameterResolvers) {
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            if (parameterResolvers[i] == null) {
                throw new UnsupportedHandlerException(
                        format("On method %s, parameter %s is invalid. It is not of any format supported by a provided"
                                       + "ParameterValueResolver.",
                               method.toGenericString(), i + 1), method);
            }
        }

        /* special case: methods with equal signature on EventListener must be rejected,
           because it interferes with the Proxy mechanism */
        if (method.getName().equals("handle")
                && Arrays.equals(method.getParameterTypes(), new Class[]{EventProxy.class})) {
            throw new UnsupportedHandlerException(String.format(
                    "Event Handling class %s contains method %s that has a naming conflict with a "
                            + "method on the EventHandler interface. Please rename the method.",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName()), method);
        }
    }

    @SuppressWarnings("rawtypes")
	private MethodMessageHandler(Method method, ParameterResolver[] parameterValueResolvers, Class payloadType) {
        super(payloadType, method.getDeclaringClass(), parameterValueResolvers);
        this.method = method;
    }

    /**
     * 返回执行的方法名称
     *
     * @return the name of the method backing this handler
     */
    public String getMethodName() {
        return method.getName();
    }

    /**
     * 返回 执行的 方法
     *
     * @return the Method backing this handler
     */
    public Method getMethod() {
        return method;
    }


    @Override
    public String toString() {
        return format("HandlerMethod %s.%s for payload type %s: %s",
                      method.getDeclaringClass().getSimpleName(), method.getName(),
                      getPayloadType().getSimpleName(), method.toGenericString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        MethodMessageHandler that = (MethodMessageHandler) o;
        return method.equals(that.method);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + method.hashCode();
        return result;
    }
}
