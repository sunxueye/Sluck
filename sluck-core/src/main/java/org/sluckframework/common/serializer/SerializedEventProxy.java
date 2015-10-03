package org.sluckframework.common.serializer;

import org.joda.time.DateTime;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.aggregate.GenericEvent;

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

    private final Object identifier;
    private final LazyDeserializingObject<T> serializedPayload;

    public SerializedEventProxy(Object eventIdentifier, DateTime timestamp,
    		SerializedObject<?> serializedPayload,Serializer serializer) {
        this.identifier = eventIdentifier;
        this.serializedPayload = new LazyDeserializingObject<>(serializedPayload, serializer);
        this.timestamp = timestamp;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public <R> SerializedObject<R> serializePayload(Serializer serializer, Class<R> expectedRepresentation) {
    	if (serializer.equals(serializedPayload.getSerializer())) {
            final SerializedObject serializedObject = serializedPayload.getSerializedObject();
            return CONVERTER_FACTORY.getConverter(serializedObject.getContentType(), expectedRepresentation)
                    .convert(serializedObject);
        }
        return serializer.serialize(serializedPayload.getObject(), expectedRepresentation);
    }

    @Override
    public Object getIdentifier() {
        return identifier;
    }

    @Override
    public DateTime occurredOn() {
        return timestamp;
    }

    @Override
    public T getPayload() {
        return serializedPayload.getObject();
    }

	@Override
    public Class<?> getPayloadType() {
        return serializedPayload.getType();
    }

    public boolean isPayloadDeserialized() {
        return serializedPayload.isDeserialized();
    }

    protected Object writeReplace() {
        return new GenericEvent<>(getIdentifier(), getPayload(), getPayloadType(), occurredOn());
    }

}
