package org.sluckframework.cqrs.commandhandling.annotation;

import org.sluckframework.common.annotation.AbstractMessageHandler;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.annotation.UnsupportedHandlerException;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.ParameterResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;
import static org.sluckframework.common.util.ReflectionUtils.ensureAccessible;

/**
 * 构造函数命令解析器,当命令处理的注解在构造函数上的时候使用,表明需要创建聚合
 * <p>
 * Author: sunxy
 * Created: 2015-09-10 11:26
 * Since: 1.0
 */
public final class ConstructorCommandHandler<T extends AggregateRoot> extends AbstractMessageHandler {

    private final Constructor<T> constructor;

    /**
     * 使用给定的 构造函数 和参数解析工厂 来创建 命令处理器
     *
     * @param constructor              The constructor to wrap as a Handler
     * @param parameterResolverFactory The strategy for resolving parameter values of handler methods
     * @param <T>                      The type of Aggregate created by the constructor
     * @return ConstructorCommandHandler
     */
    public static <T extends AggregateRoot> ConstructorCommandHandler<T> forConstructor(
            Constructor<T> constructor, ParameterResolverFactory parameterResolverFactory) {
        ParameterResolver[] resolvers = findResolvers(parameterResolverFactory,
                constructor.getAnnotations(),
                constructor.getParameterTypes(),
                constructor.getParameterAnnotations(),
                true);
        Class<?> firstParameter = constructor.getParameterTypes()[0];
        Class payloadType;
        if (EventProxy.class.isAssignableFrom(firstParameter)) {
            payloadType = Object.class;
        } else {
            payloadType = firstParameter;
        }
        ensureAccessible(constructor);
        validate(constructor, resolvers);
        return new ConstructorCommandHandler<>(constructor, resolvers, payloadType);
    }

    private static void validate(Constructor constructor, ParameterResolver[] parameterResolvers) {
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            if (parameterResolvers[i] == null) {
                throw new UnsupportedHandlerException(
                        format("On method %s, parameter %s is invalid. It is not of any format supported by a provided"
                                        + "ParameterValueResolver.",
                                constructor.toGenericString(), i + 1), constructor);
            }
        }
    }

    /**
     * 使用给定的 构造函数 和 参数解析器 ,接受指定的 参数类型 来初始化
     *
     * @param constructor             The constructor this handler should invoke
     * @param parameterValueResolvers The resolvers for the constructor parameters
     * @param payloadType             The payload type the constructor is assigned to handle
     */
    private ConstructorCommandHandler(Constructor<T> constructor, ParameterResolver[] parameterValueResolvers,
                                      Class payloadType) {
        super(payloadType, constructor.getDeclaringClass(), parameterValueResolvers);
        this.constructor = constructor;
    }

    @Override
    public T invoke(Object target, EventProxy message) throws InvocationTargetException, IllegalAccessException {
        Object[] parameterValues = new Object[getParameterValueResolvers().length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = getParameterValueResolvers()[i].resolveParameterValue(message);
        }
        try {
            return constructor.newInstance(parameterValues);
        } catch (InstantiationException e) {
            throw new InvocationTargetException(e.getCause()); // NOSONAR
        }
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return constructor.getAnnotation(annotationType);
    }
}

