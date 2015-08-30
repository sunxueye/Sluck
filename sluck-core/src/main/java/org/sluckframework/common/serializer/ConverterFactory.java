package org.sluckframework.common.serializer;


/**
 * 提供转换实例 来对 源类型和目标类型的转换
 * 
 * @author sunxy
 * @time 2015年8月29日 下午6:43:47
 * @since 1.0
 */
public interface ConverterFactory {
	
	/**
	 * 判断 是否包含 可以对指定源类型和目标类型转换的 converter
     *
     * @param sourceContentType source
     * @param targetContentType target
     * @return true if a converter is available, otherwise false
     */
    <S, T> boolean hasConverter(Class<S> sourceContentType, Class<T> targetContentType);

    /**
     * 获取可以堆指定 源类型和目标类型进行转换的 converter
     *
     * @param sourceContentType source
     * @param targetContentType target
     * @return a converter capable of converting s and t
     *
     * @throws CannotConvertBetweenTypesException
     */
    <S, T> ContentTypeConverter<S, T> getConverter(Class<S> sourceContentType, Class<T> targetContentType);

}
