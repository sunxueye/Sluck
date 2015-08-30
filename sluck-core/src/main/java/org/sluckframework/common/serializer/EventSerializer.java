package org.sluckframework.common.serializer;

import org.sluckframework.common.exception.Assert;
import org.sluckframework.domain.event.EventProxy;

/**
 * 提供对 event 的序列化支持，
 * 
 * @author sunxy
 * @time 2015年8月29日 下午6:18:07
 * @since 1.0
 */
public class EventSerializer implements Serializer {

	private final Serializer serializer;

	/**
	 * 使用指定 的 serializer进行初始化
	 *
	 * @param serializer serializer event and deserilaizer
	 */
	public EventSerializer(Serializer serializer) {
		Assert.notNull(serializer, "serializer may not be null");
		this.serializer = serializer;
	}

	/**
	 * 序列化 event中的 payload，该方法会验证 eventMessage是否 实现了SerializationAware 接口，
	 * 如果实现了则由其自己序列化
	 *
	 * @param event The message containing the payload to serialize
	 * @param serializer The serializer to serialize the payload 
	 * @param expectedRepresentation The data type to serialize to
	 * 
	 * @return a Serialized Object containing the serialized for of the event's payload
	 */
	public static <T> SerializedObject<T> serializePayload(EventProxy<?> event,Serializer serializer, 
			Class<T> expectedRepresentation) {
		if (event instanceof SerializationAware) {
			return ((SerializationAware) event).serializePayload(serializer, expectedRepresentation);
		}
		return serializer.serialize(event.getPayload(),
				expectedRepresentation);
	}


	/**
	 * 序列化指定的eventProxy 的 playload,到指定类型
	 *
	 * @param event The event containing the payload to serialize
	 * @param expectedRepresentation expected type
	 * @return A serialized object containing the serialized representation of
	 *         the message's payload
	 */
	public <T> SerializedObject<T> serializePayload(EventProxy<?> event,
			Class<T> expectedRepresentation) {
		return serializePayload(event, serializer, expectedRepresentation);
	}

	@Override
	public <T> SerializedObject<T> serialize(Object object,
			Class<T> expectedRepresentation) {
		return serializer.serialize(object, expectedRepresentation);
	}

	@Override
	public <T> boolean canSerializeTo(Class<T> expectedRepresentation) {
		return serializer.canSerializeTo(expectedRepresentation);
	}

	@Override
	public <S, T> T deserialize(SerializedObject<S> serializedObject) {
		return serializer.deserialize(serializedObject);
	}

	@Override
	public Class<?> classForType(SerializedType type) {
		return serializer.classForType(type);
	}

	@Override
	public SerializedType typeForClass(Class<?> type) {
		return serializer.typeForClass(type);
	}

	@Override
	public ConverterFactory getConverterFactory() {
		return serializer.getConverterFactory();
	}

}
