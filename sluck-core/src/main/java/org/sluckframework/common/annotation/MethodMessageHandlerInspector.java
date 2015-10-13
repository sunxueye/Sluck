package org.sluckframework.common.annotation;

import org.sluckframework.domain.event.EventProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.sluckframework.common.util.ReflectionUtils.methodsOf;


/**
 * 根据指定 handlerDefiner 找到目标类中  对应的 执行方法 
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:14:23	
 * @since 1.0
 */
public class MethodMessageHandlerInspector {

    private final Class<?> targetType;
    private final List<MethodMessageHandler> handlers = new ArrayList<MethodMessageHandler>();
    private final ParameterResolverFactory parameterResolver;

    private static final ConcurrentMap<String, MethodMessageHandlerInspector> INSPECTORS =
            new ConcurrentHashMap<String, MethodMessageHandlerInspector>();

    /**
     * 给给定的 handlerClass 返回 MethodMessageHandlerInspector
     *
     * @param handlerClass             The Class containing the handler methods to evaluate
     * @param annotationType           The annotation marking handler methods
     * @param parameterResolverFactory The strategy for resolving parameter value for handler methods
     * @param allowDuplicates          Indicates whether to accept multiple handlers listening to Messages with the
     *                                 same payload type
     * @return a MethodMessageHandlerInspector providing access to the handler methods
     */
    public static <T extends Annotation> MethodMessageHandlerInspector getInstance(
            Class<?> handlerClass, Class<T> annotationType, ParameterResolverFactory parameterResolverFactory,
            boolean allowDuplicates) {
        return getInstance(handlerClass, parameterResolverFactory, allowDuplicates,
                           new AnnotatedHandlerDefinition<T>(annotationType));
    }

    /**
     * 给给定的 handlerClass 返回 MethodMessageHandlerInspector
     *
     * @param handlerClass             The Class containing the handler methods to evaluate
     * @param parameterResolverFactory The strategy for resolving parameter value for handler methods
     * @param allowDuplicates          Indicates whether to accept multiple handlers listening to Messages with the
     *                                 same payload type
     * @param handlerDefinition        The definition indicating which methods are message handlers
     * @return a MethodMessageHandlerInspector providing access to the handler methods
     */
    public static MethodMessageHandlerInspector getInstance(Class<?> handlerClass,
                                                            ParameterResolverFactory parameterResolverFactory,
                                                            boolean allowDuplicates,
                                                            HandlerDefinition<? super Method> handlerDefinition) {
        String key = handlerDefinition.toString() + "@" + handlerClass.getName();
        MethodMessageHandlerInspector inspector = INSPECTORS.get(key);
        while (inspector == null
                || !handlerClass.equals(inspector.getTargetType())
                || !inspector.parameterResolver.equals(parameterResolverFactory)) {
            final MethodMessageHandlerInspector newInspector = new MethodMessageHandlerInspector(
                    parameterResolverFactory,
                    handlerClass,
                    allowDuplicates,
                    handlerDefinition);
            if (inspector == null) {
                INSPECTORS.putIfAbsent(key, newInspector);
            } else {
                INSPECTORS.replace(key, inspector, newInspector);
            }
            inspector = INSPECTORS.get(key);
        }
        return inspector;
    }

    public static void clearCache(){
        INSPECTORS.clear();
    }
    
    private MethodMessageHandlerInspector(ParameterResolverFactory parameterResolverFactory,
                                          Class<?> targetType, boolean allowDuplicates,
                                          HandlerDefinition<? super Method> handlerDefinition) {
        this.parameterResolver = parameterResolverFactory;
        this.targetType = targetType;
        Iterable<Method> methods = methodsOf(targetType);
        NavigableSet<MethodMessageHandler> uniqueHandlers = new TreeSet<>();
        for (Method method : methods) {
            if (handlerDefinition.isMessageHandler(method)) {
                final Class<?> explicitPayloadType = handlerDefinition.resolvePayloadFor(method);
                MethodMessageHandler handlerMethod = MethodMessageHandler.createFor(method,
                                                                                    explicitPayloadType,
                                                                                    parameterResolverFactory
                );
                handlers.add(handlerMethod);
                if (!allowDuplicates && !uniqueHandlers.add(handlerMethod)) {
                    MethodMessageHandler existing = uniqueHandlers.tailSet(handlerMethod).first();
                    throw new UnsupportedHandlerException(
                            String.format("The class %s contains two handler methods (%s and %s) that listen "
                                                  + "to the same Message type: %s",
                                          method.getDeclaringClass().getSimpleName(),
                                          handlerMethod.getMethodName(),
                                          existing.getMethodName(),
                                          handlerMethod.getPayloadType().getSimpleName()), method);
                }
            }
        }
        Collections.sort(handlers);
    }

    /**
     * 根据给定的参数类型 返回 handler method
     *
     * @param message The message to find a handler for
     * @return the  handler method for the given parameterType
     */
    public MethodMessageHandler findHandlerMethod(final EventProxy<?> message) {
        for (MethodMessageHandler handler : handlers) {
            if (handler.matches(message)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Returns the list of handlers found on target type.
     *
     * @return the list of handlers found on target type
     */
    public List<MethodMessageHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }

    /**
     * Returns the targetType on which handler methods are invoked.
     *
     * @return the targetType on which handler methods are invoked
     */
    public Class<?> getTargetType() {
        return targetType;
    }

    private static class AnnotatedHandlerDefinition<T extends Annotation>
            extends AbstractAnnotatedHandlerDefinition<T> {

        protected AnnotatedHandlerDefinition(Class<T> annotationType) {
            super(annotationType);
        }

        @Override
        protected Class<?> getDefinedPayload(T annotation) {
            return null;
        }
    }

}
