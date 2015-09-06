package org.sluckframework.cqrs.eventhanding.annotation;

import static org.sluckframework.common.util.CollectionUtils.getAnnotation;

import java.lang.annotation.Annotation;

import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.util.ReflectionUtils;
import org.sluckframework.domain.event.ParameterResolver;


/**
 * @author sunxy
 * @time 2015年9月6日 下午8:09:42
 * @since 1.0
 */
public abstract class AbstractAnnotatedParameterResolverFactory<A, P> implements ParameterResolverFactory {

    private final Class<A> annotationType;
    private final Class<P> declaredParameterType;

    /**
     * Initialize a ParameterResolverFactory instance that resolves parameters of type
     * <code>declaredParameterType</code> annotated with the given <code>annotationType</code>.
     *
     * @param annotationType        the type of annotation that a prospective parameter should declare
     * @param declaredParameterType the type that the parameter value should be assignable to
     */
    protected AbstractAnnotatedParameterResolverFactory(Class<A> annotationType, Class<P> declaredParameterType) {
        Assert.notNull(annotationType, "annotationType may not be null");
        Assert.notNull(declaredParameterType, "declaredParameterType may not be null");
        this.annotationType = annotationType;
        this.declaredParameterType = declaredParameterType;
    }

    /**
     * @return the parameter resolver that is supplied when a matching parameter is located
     */
    protected abstract ParameterResolver<P> getResolver();

    @SuppressWarnings("rawtypes")
	@Override
    public ParameterResolver createInstance(Annotation[] memberAnnotations, Class<?> parameterType,
                                            Annotation[] parameterAnnotations) {
        A annotation = getAnnotation(parameterAnnotations, annotationType);
        if (annotation != null) {
            if (parameterType.isAssignableFrom(declaredParameterType)) {
                return getResolver();
            }

            //a 2nd chance to resolve if the parameter is primitive but its boxed wrapper type is assignable
            if (parameterType.isPrimitive()
                    && ReflectionUtils.resolvePrimitiveWrapperType(parameterType)
                                      .isAssignableFrom(declaredParameterType)) {
                return getResolver();
            }
        }

        return null;
    }
}

