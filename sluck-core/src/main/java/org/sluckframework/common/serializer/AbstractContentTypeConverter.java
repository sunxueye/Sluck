package org.sluckframework.common.serializer;


/**
 * ContentTypeConverter的抽象基类
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:03:02
 * @since 1.0
 */
public abstract class AbstractContentTypeConverter<S, T> implements ContentTypeConverter<S, T> {
	
	@Override
    public SerializedObject<T> convert(SerializedObject<S> original) {
        return new SimpleSerializedObject<>(convert(original.getData()), targetType(), original.getType());
    }

}
