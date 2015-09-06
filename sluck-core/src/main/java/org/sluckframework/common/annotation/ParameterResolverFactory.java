package org.sluckframework.common.annotation;

import java.lang.annotation.Annotation;

import org.sluckframework.domain.event.ParameterResolver;


/**
 * 参数解析工厂，可以使用自定义的，使用ServiceLoader 机制
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:59:40	
 * @since 1.0
 */
public interface ParameterResolverFactory {

    /**
     * 创建 参数解析器 解析 注解 参数
     *
     * @param memberAnnotations    annotations placed on the member
     * @param parameterType        the parameter type to find a resolver for
     * @param parameterAnnotations annotations placed on the parameter
     * @return a suitable ParameterResolver, or <code>null</code> if none is found
     */
    @SuppressWarnings("rawtypes")
	ParameterResolver createInstance(Annotation[] memberAnnotations, Class<?> parameterType,
                                     Annotation[] parameterAnnotations);

}
