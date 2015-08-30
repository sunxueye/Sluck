package org.sluckframework.common.serializer;

import java.util.Map;

import org.joda.time.DateTime;
import org.sluckframework.domain.event.EventProxy;


/**
 * 序列化的 事件 in信息
 * 
 * @author sunxy
 * @time 2015年8月31日 上午12:39:05
 * @since 1.0
 */
public class SerializedEventProxy<T> implements EventProxy<T>, SerializationAware {

	private static final long serialVersionUID = 4084002795375211384L;
	
	private static final ConverterFactory CONVERTER_FACTORY = new ChainingConverterFactory();
    private final DateTime timestamp;

    private final String identifier;
    private final LazyDeserializingObject<T> serializedPayload;

    public SerializedEventProxy(String eventIdentifier, DateTime timestamp,
    		SerializedObject<?> serializedPayload,Serializer serializer) {
        this.identifier = eventIdentifier;
        this.serializedPayload = new LazyDeserializingObject<T>(serializedPayload, serializer);
        this.timestamp = timestamp;
    }
    
    public <R> SerializedObject<R> serializePayload(Serializer serializer, Class<R> expectedRepresentation) {
        if (serializer.equals(serializedPayload.getSerializer())) {
            final SerializedObject serializedObject = serializedPayload.getSerializedObject();
            return CONVERTER_FACTORY.getConverter(serializedObject.getContentType(), expectedRepresentation)
                    .convert(serializedObject);
        }
        return serializer.serialize(serializedPayload.getObject(), expectedRepresentation);
    }

    @Override
    public <R> SerializedObject<R> serializePayload(Serializer serializer, Class<R> expectedRepresentation) {
        return message.serializePayload(serializer, expectedRepresentation);
    }

    @Override
    public <R> SerializedObject<R> serializeMetaData(Serializer serializer, Class<R> expectedRepresentation) {
        return message.serializeMetaData(serializer, expectedRepresentation);
    }

    @Override
    public String getIdentifier() {
        return message.getIdentifier();
    }

    @Override
    public DateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public MetaData getMetaData() {
        return message.getMetaData();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public T getPayload() {
        return message.getPayload();
    }

    @Override
    public Class getPayloadType() {
        return message.getPayloadType();
    }

    public boolean isPayloadDeserialized() {
        return message.isPayloadDeserialized();
    }

    protected Object writeReplace() {
        return new GenericEventMessage<T>(getIdentifier(), getTimestamp(), getPayload(), getMetaData());
    }

}
