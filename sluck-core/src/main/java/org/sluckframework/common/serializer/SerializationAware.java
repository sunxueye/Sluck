package org.sluckframework.common.serializer;

/**
 * 表明接口有序列化的能力
 * 
 * @author sunxy
 * @time 2015年8月29日 下午7:04:24
 * @since 1.0
 */
public interface SerializationAware {
	
	/**
	 * 使用指定的 serializer 序列化 eventProxy 的 payload，序列化为期望的类型
	 * 当使用相同的 serializer的时候，应该返回相同的 SerializedObject实例
     *
     * @param serializer             The serializer to serialize payload with
     * @param expectedRepresentation The type of data to serialize to
     * @return a SerializedObject 
     */
    <T> SerializedObject<T> serializePayload(Serializer serializer, Class<T> expectedRepresentation);
}
