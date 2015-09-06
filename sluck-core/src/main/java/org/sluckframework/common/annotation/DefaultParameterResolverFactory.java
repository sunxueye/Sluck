package org.sluckframework.common.annotation;


import java.lang.annotation.Annotation;

import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.ParameterResolver;


/**
 * 默认的参数解析器
 * 
 * @author sunxy
 * @time 2015年9月6日 下午8:18:22
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Priority.FIRST)
public class DefaultParameterResolverFactory implements ParameterResolverFactory {

    @Override
    public ParameterResolver createInstance(Annotation[] methodAnnotations, Class<?> parameterType,
                                            Annotation[] parameterAnnotations) {
        if (EventProxy.class.isAssignableFrom(parameterType)) {
            return new MessageParameterResolver(parameterType);
        }
//        if (getAnnotation(parameterAnnotations, MetaData.class) != null) {
//            return new AnnotatedMetaDataParameterResolver(CollectionUtils.getAnnotation(parameterAnnotations,
//                                                                                        MetaData.class), parameterType);
//        }
//        if (org.axonframework.domain.MetaData.class.isAssignableFrom(parameterType)) {
//            return MetaDataParameterResolver.INSTANCE;
//        }
        return null;
    }

//    private static class AnnotatedMetaDataParameterResolver implements ParameterResolver {
//
//        private final MetaData metaData;
//        private final Class parameterType;
//
//        public AnnotatedMetaDataParameterResolver(MetaData metaData, Class parameterType) {
//            this.metaData = metaData;
//            this.parameterType = parameterType;
//        }
//
//        @Override
//        public Object resolveParameterValue(Message message) {
//            return message.getMetaData().get(metaData.value());
//        }
//
//        @Override
//        public boolean matches(Message message) {
//            return !(parameterType.isPrimitive() || metaData.required())
//                    || (
//                    message.getMetaData().containsKey(metaData.value())
//                            && parameterType.isInstance(message.getMetaData().get(metaData.value()))
//            );
//        }
//    }
//
//    private static final class MetaDataParameterResolver implements ParameterResolver {
//
//        private static final MetaDataParameterResolver INSTANCE = new MetaDataParameterResolver();
//
//        private MetaDataParameterResolver() {
//        }
//
//        @Override
//        public Object resolveParameterValue(Message message) {
//            return message.getMetaData();
//        }
//
//        @Override
//        public boolean matches(Message message) {
//            return true;
//        }
//    }

    private static class MessageParameterResolver implements ParameterResolver {

        private final Class<?> parameterType;

        public MessageParameterResolver(Class<?> parameterType) {
            this.parameterType = parameterType;
        }

        @Override
        public Object resolveParameterValue(EventProxy message) {
            return message;
        }

		@Override
        public boolean matches(EventProxy message) {
            return parameterType.isInstance(message);
        }
    }
}