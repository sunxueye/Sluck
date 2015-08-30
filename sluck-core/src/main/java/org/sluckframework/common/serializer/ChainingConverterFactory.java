package org.sluckframework.common.serializer;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用多个 coverterFactory来组合链 去 coverter 
 * 此实现会是哟个ServiceLoader 机制来 加载 /META-INF/services/org.sluckframework.serializer.ContentTypeConverter
 * 文件读取converter,文件中定义的class 必须是全限定名称
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:45:41
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class ChainingConverterFactory implements ConverterFactory {

    private static final Logger logger = LoggerFactory.getLogger(ChainingConverterFactory.class);
    private final List<ContentTypeConverter<?, ?>> converters = new CopyOnWriteArrayList<ContentTypeConverter<?, ?>>();

    /**
     * 初始化，会加载/META-INF/services/org.sluckframework.serializer.ContentTypeConverter
     */
	public ChainingConverterFactory() {
        ServiceLoader<ContentTypeConverter> converterLoader = ServiceLoader.load(ContentTypeConverter.class);
        for (ContentTypeConverter converter : converterLoader) {
            converters.add(converter);
        }
    }

    @Override
    public <S, T> boolean hasConverter(Class<S> sourceContentType, Class<T> targetContentType) {
        if (sourceContentType.equals(targetContentType)) {
            return true;
        }
        for (ContentTypeConverter converter : converters) {
            if (canConvert(converter, sourceContentType, targetContentType)) {
                return true;
            }
        }
        return ChainedConverter.canConvert(sourceContentType, targetContentType, converters);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S, T> ContentTypeConverter<S, T> getConverter(Class<S> sourceContentType, Class<T> targetContentType) {
        if (sourceContentType.equals(targetContentType)) {
            return new NoConversion(sourceContentType);
        }
        for (ContentTypeConverter converter : converters) {
            if (canConvert(converter, sourceContentType, targetContentType)) {
                return converter;
            }
        }
        ChainedConverter<S, T> converter = ChainedConverter.calculateChain(sourceContentType, targetContentType,
                                                                           converters);
        converters.add(0, converter);
        return converter;
    }

    private <S, T> boolean canConvert(ContentTypeConverter<?, ?> converter, Class<S> sourceContentType,
                                      Class<T> targetContentType) {
        try {
            if (converter.expectedSourceType().isAssignableFrom(sourceContentType)
                    && targetContentType.isAssignableFrom(converter.targetType())) {
                return true;
            }
            // we do this call to make sure target Type is on the classpath
            converter.targetType();
        } catch (NoClassDefFoundError e) {
            logger.info("ContentTypeConverter [{}] is ignored. It seems to rely on a class that is "
                                + "not available in the class loader: {}", converter, e.getMessage());
            converters.remove(converter);
        }
        return false;
    }

    /**
     * 注册给定的 converter 到第一个
     * 
     * @param converter the converter to register.
     */
    public void registerConverter(ContentTypeConverter converter) {
        converters.add(0, converter);
    }

    /**
     * 注册给定的类型的 converter,使用默认构造参数 构造对象
     *
     * @param converterType the type of converter to register.
     */
    public void registerConverter(Class<? extends ContentTypeConverter> converterType) {
        try {
            ContentTypeConverter converter = converterType.getConstructor().newInstance();
            converter.targetType();
            converter.expectedSourceType();
            registerConverter(converter);
        } catch (Exception e) {
            logger.warn("An exception occurred while trying to initialize a [{}].", converterType.getName(), e);
        } catch (NoClassDefFoundError e) {
            logger.info("ContentTypeConverter of type [{}] is ignored. It seems to rely on a class that is "
                                + "not available in the class loader: {}", converterType, e.getMessage());
        }
    }

    /**
     * 将指定的 converters 加入到 已有的List中
     *
     * @param additionalConverters The converters to register with this factory
     */
    public void setAdditionalConverters(List<ContentTypeConverter> additionalConverters) {
        for (ContentTypeConverter converter : additionalConverters) {
            registerConverter(converter);
        }
    }

    private static class NoConversion<T> implements ContentTypeConverter<T, T> {

        private final Class<T> type;

        public NoConversion(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<T> expectedSourceType() {
            return type;
        }

        @Override
        public Class<T> targetType() {
            return type;
        }

        @Override
        public SerializedObject<T> convert(SerializedObject<T> original) {
            return original;
        }

        @Override
        public T convert(T original) {
            return original;
        }
    }

}
