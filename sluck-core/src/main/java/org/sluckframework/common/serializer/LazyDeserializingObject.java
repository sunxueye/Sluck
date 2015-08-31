package org.sluckframework.common.serializer;

import org.sluckframework.common.exception.Assert;

/**
 * 延迟 反序列化 对象
 * 
 * @author sunxy
 * @time 2015年8月31日 上午9:37:15	
 * @since 1.0
 */
public class LazyDeserializingObject<T> {
	
    private final Serializer serializer;
    private final SerializedObject<?> serializedObject;
    private final Class<?> deserializedObjectType;
    private volatile T deserializedObject;

    /**
     * 使用已 反序列化 的对象 进行 初始化
     *
     * @param deserializedObject The deserialized object to return
     */
    public LazyDeserializingObject(T deserializedObject) {
        Assert.notNull(deserializedObject, "The given deserialized instance may not be null");
        this.serializedObject = null;
        this.serializer = null;
        this.deserializedObject = deserializedObject;
        this.deserializedObjectType = deserializedObject.getClass();
    }

    /**
     * 使用 序列化对象 和 序列化 转换 初始化
     * 
     * @param serializedObject The serialized payload of the message
     * @param serializer       The serializer to deserialize the payload data with
     */
    public LazyDeserializingObject(SerializedObject<?> serializedObject, Serializer serializer) {
        Assert.notNull(serializedObject, "The given serializedObject may not be null");
        Assert.notNull(serializer, "The given serializer may not be null");
        this.serializedObject = serializedObject;
        this.serializer = serializer;
        this.deserializedObjectType = serializer.classForType(serializedObject.getType());
    }

    /**
     * 返回 被序列化对象的 原始类型
     *
     * @return the class of the serialized object
     */
    public Class<?> getType() {
        return deserializedObjectType;
    }

    /**
     * 进行 反序列化 并 返回 结果
     *
     * @return the deserialized objects
     */
    public T getObject() {
        if (!isDeserialized()) {
            deserializedObject = serializer.deserialize(serializedObject);
        }
        return deserializedObject;
    }

    /**
     * 判断 是否已经 反序列化
     *
     * @return whether the contained object has been deserialized already.
     */
    public boolean isDeserialized() {
        return deserializedObject != null;
    }

    /**
     * 序列化者
     *
     * @return the serializer to deserialize this object
     */
    public Serializer getSerializer() {
        return serializer;
    }

    /**
     * 序列化对象
     *
     * @return the serialized object to deserialized
     */
    public SerializedObject<?> getSerializedObject() {
        return serializedObject;
    }

}
