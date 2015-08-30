package org.sluckframework.common.serializer;

/**
 * 可以对源类型和目标类型转换的转换器
 * 
 * @author sunxy
 * @time 2015年8月29日 下午6:48:54
 * @since 1.0
 */
public interface ContentTypeConverter<S, T> {
	
	/**
     * 源类型.
     *
     * @return source type
     */
    Class<S> expectedSourceType();

    /**
     * 目标类型
     *
     * @return target type
     */
    Class<T> targetType();

    /**
     * 将包含源 类型的 SerializedObject转换为 目标类型 SerializedObject
     *
     * @param original The source to convert
     * @return the converted representation
     */
    SerializedObject<T> convert(SerializedObject<S> original);

    /**
     * 将源类型转换为目标类型
     *
     * @param original the value to convert
     * @return the converted value
     */
    T convert(S original);

}
