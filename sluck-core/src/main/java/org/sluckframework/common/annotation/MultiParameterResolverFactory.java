package org.sluckframework.common.annotation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sluckframework.domain.event.ParameterResolver;

/**
 * 委托 其他多个 实例工厂 处理，支持 Order
 * 
 * @author sunxy
 * @time 2015年9月6日 下午4:25:55	
 * @since 1.0
 */
public class MultiParameterResolverFactory implements ParameterResolverFactory {

    private final ParameterResolverFactory[] factories;

    public static MultiParameterResolverFactory ordered(ParameterResolverFactory... delegates) {
        return ordered(Arrays.asList(delegates));
    }

    public static MultiParameterResolverFactory ordered(List<ParameterResolverFactory> delegates) {
        return new MultiParameterResolverFactory(flatten(delegates));
    }

    public MultiParameterResolverFactory(ParameterResolverFactory... delegates) {
        this.factories = Arrays.copyOf(delegates, delegates.length);
    }

    public MultiParameterResolverFactory(List<ParameterResolverFactory> delegates) {
        this.factories = delegates.toArray(new ParameterResolverFactory[delegates.size()]);
    }

    private static ParameterResolverFactory[] flatten(List<ParameterResolverFactory> factories) {
        List<ParameterResolverFactory> flattened = new ArrayList<ParameterResolverFactory>(factories.size());
        for (ParameterResolverFactory parameterResolverFactory : factories) {
            if (parameterResolverFactory instanceof MultiParameterResolverFactory) {
                flattened.addAll(((MultiParameterResolverFactory) parameterResolverFactory).getDelegates());
            } else {
                flattened.add(parameterResolverFactory);
            }
        }
        Collections.sort(flattened, PriorityAnnotationComparator.getInstance());
        return flattened.toArray(new ParameterResolverFactory[flattened.size()]);
    }

    /**
     * Returns the delegates of this instance, in the order they are evaluated to resolve parameters.
     *
     * @return the delegates of this instance, in the order they are evaluated to resolve parameters
     */
    public List<ParameterResolverFactory> getDelegates() {
        return Arrays.asList(factories);
    }

    @SuppressWarnings("rawtypes")
	@Override
    public ParameterResolver createInstance(Annotation[] memberAnnotations, Class<?> parameterType,
                                            Annotation[] parameterAnnotations) {
        for (ParameterResolverFactory factory : factories) {
            ParameterResolver resolver = factory.createInstance(memberAnnotations, parameterType, parameterAnnotations);
            if (resolver != null) {
                return resolver;
            }
        }
        return null;
    }
}
