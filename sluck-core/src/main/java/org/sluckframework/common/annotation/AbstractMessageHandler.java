package org.sluckframework.common.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.ParameterResolver;


/**
 * 消息处理
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:16:01	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractMessageHandler implements Comparable<AbstractMessageHandler> {

    private final Score score;
    private final Class<?> payloadType;
    private final ParameterResolver[] parameterValueResolvers;

    /**
     * 初始化 消息处理 用给定的 参数
     *
     * @param payloadType             The type of payload this handlers deals with
     * @param declaringClass          The class on which the handler is declared
     * @param parameterValueResolvers The resolvers for each of the handlers' parameters
     */
    
	protected AbstractMessageHandler(Class<?> payloadType, Class<?> declaringClass,
                                     ParameterResolver... parameterValueResolvers) {
        this.score = new Score(payloadType, declaringClass);
        this.payloadType = payloadType;
        this.parameterValueResolvers = Arrays.copyOf(parameterValueResolvers, parameterValueResolvers.length);
    }

    protected AbstractMessageHandler(AbstractMessageHandler delegate) {
        this.score = delegate.score;
        this.payloadType = delegate.payloadType;
        this.parameterValueResolvers = delegate.parameterValueResolvers;
    }

    /**
     * 判断 消息处理是否 匹配 指定消息
     *
     * @param message The message to inspect
     * @return <code>true</code> if this handler can handle the message, otherwise <code>false</code>.
     */
    public boolean matches(EventProxy<?> message) {
        Assert.notNull(message, "Event may not be null");
        if (payloadType != null && !payloadType.isAssignableFrom(message.getPayloadType())) {
            return false;
        }
        for (ParameterResolver parameterResolver : parameterValueResolvers) {
            if (!parameterResolver.matches(message)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据 eventProxy提供的参数 执行 指定方法
     * 
     * @param target  The target instance to invoke the Handler on.
     * @param message The message providing parameter values
     * @return The result of the handler invocation
     */
    public abstract Object invoke(Object target, EventProxy<?> message)
            throws InvocationTargetException, IllegalAccessException;

    /**
     * Returns the type of payload this handler expects.
     *
     * @return the type of payload this handler expects
     */
    public Class getPayloadType() {
        return payloadType;
    }

    @Override
    public int compareTo(AbstractMessageHandler o) {
        return score.compareTo(o.score);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof AbstractMessageHandler)
                && ((AbstractMessageHandler) obj).score.equals(score);
    }

    @Override
    public int hashCode() {
        return score.hashCode();
    }

    /**
     * 根据信息找出 指定  ParameterResolvers
     *
     * @param parameterResolverFactory The factory to create the ParameterResolvers with
     * @param memberAnnotations        The annotations on the member (e.g. method)
     * @param parameterTypes           The parameter type of the member
     * @param parameterAnnotations     The annotations on each of the parameters
     * @param resolvePayload           Indicates whether the payload of the message should be resolved from the
     *                                 parameters
     * @return the parameter resolvers for the given Member details
     */
    protected static ParameterResolver[] findResolvers(ParameterResolverFactory parameterResolverFactory,
                                                       Annotation[] memberAnnotations, Class<?>[] parameterTypes,
                                                       Annotation[][] parameterAnnotations, boolean resolvePayload) {
        int parameters = parameterTypes.length;
        ParameterResolver[] parameterValueResolvers = new ParameterResolver[parameters];
        for (int i = 0; i < parameters; i++) {
            // currently, the first parameter is considered the payload parameter
            final boolean isPayloadParameter = resolvePayload && i == 0;
            if (isPayloadParameter && !EventProxy.class.isAssignableFrom(parameterTypes[i])) {
                parameterValueResolvers[i] = new PayloadParameterResolver(parameterTypes[i]);
            } else {
                parameterValueResolvers[i] = parameterResolverFactory.createInstance(memberAnnotations,
                                                                                     parameterTypes[i],
                                                                                     parameterAnnotations[i]);
            }
        }
        return parameterValueResolvers;
    }

    protected ParameterResolver[] getParameterValueResolvers() {
        return parameterValueResolvers;
    }

    public abstract <T extends Annotation> T getAnnotation(Class<T> annotationType);

    private static class PayloadParameterResolver implements ParameterResolver {

        private final Class<?> payloadType;

        public PayloadParameterResolver(Class<?> payloadType) {
            this.payloadType = payloadType;
        }

        @Override
        public Object resolveParameterValue(EventProxy message) {
            return message.getPayload();
        }

        @Override
        public boolean matches(EventProxy message) {
            return message.getPayloadType() != null && payloadType.isAssignableFrom(message.getPayloadType());
        }
    }

    private static final class Score implements Comparable<Score> {

        private final int declarationDepth;
        private final int payloadDepth;
        private final String payloadName;

        private Score(Class payloadType, Class<?> declaringClass) {
            declarationDepth = superClassCount(declaringClass, 0);
            payloadDepth = superClassCount(payloadType, -255);
            payloadName = payloadType.getName();
        }

        private int superClassCount(Class<?> declaringClass, int interfaceScore) {
            if (declaringClass.isInterface()) {
                return interfaceScore;
            }
            int superClasses = 0;

            while (declaringClass != null) {
                superClasses++;
                declaringClass = declaringClass.getSuperclass();
            }
            return superClasses;
        }

        @Override
        public int compareTo(Score o) {
            if (declarationDepth != o.declarationDepth) {
                return (o.declarationDepth < declarationDepth) ? -1 : 1;
            } else if (payloadDepth != o.payloadDepth) {
                return (o.payloadDepth < payloadDepth) ? -1 : 1;
            } else {
                return payloadName.compareTo(o.payloadName);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Score score = (Score) o;
            return declarationDepth == score.declarationDepth
                    && payloadDepth == score.payloadDepth
                    && payloadName.equals(score.payloadName);
        }

        @Override
        public int hashCode() {
            int result = declarationDepth;
            result = 31 * result + payloadDepth;
            result = 31 * result + payloadName.hashCode();
            return result;
        }
    }

}
