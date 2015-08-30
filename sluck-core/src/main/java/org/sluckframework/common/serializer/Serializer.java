package org.sluckframework.common.serializer;

/**
 * 实现序列化功能，给序列化和反序列化
 * 
 * @author sunxy
 * @time 2015年8月29日 下午6:18:28
 * @since 1.0
 */
public interface Serializer {
	
	/**
	 * 给定希望序列化后的类型序列化指定对象，返回序列化后的Serialized Object
	 * 
     * @param object The object to serialize
     * @param expectedRepresentation The expected data type
     * @return the instance representing the serialized object.
     */
    <T> SerializedObject<T> serialize(Object object, Class<T> expectedRepresentation);

    /**
     * 判断此序列化对象 能否序列化指定的类型
     * 
     * @param expectedRepresentation The type of data a Serialized Object should contain
     * @return true if the expectedRepresentation is supported, otherwise false.
     */
    <T> boolean canSerializeTo(Class<T> expectedRepresentation);

    /**
     * 反序列化
     *
     * @param serializedObject the serialized data
     * @param <S> The data type of the serialized object
     * @param <T> The expected deserialized type
     * @return the serialized object, cast to the expected type
     */
    <S, T> T deserialize(SerializedObject<S> serializedObject);

    /**
     * 获取序列化type class name，并加载class,返回加载后的class
     * 
     * @param type The type identifier of the object
     * @return the Class representing the type of the serialized Object
     */
    Class<?> classForType(SerializedType type) throws UnknownSerializedTypeException;

    /**
     * 根据class,创建相应的SerializedType
     *
     * @param target Class
     * @return The type identifier of the object
     */
    SerializedType typeForClass(Class<?> type);

    /**
     * 获取用于序列化时候转换信息的 转换工厂
     * 
     * @return the converter factory used by this Serializer
     */
    ConverterFactory getConverterFactory();

}
