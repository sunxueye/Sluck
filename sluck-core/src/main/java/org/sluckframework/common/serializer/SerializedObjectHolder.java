package org.sluckframework.common.serializer;

import java.util.HashMap;
import java.util.Map;

import org.sluckframework.domain.event.EventProxy;


/**
 * 持有序列化的 evntProxy 信息
 * 
 * @author sunxy
 * @time 2015年9月7日 下午11:28:35
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class SerializedObjectHolder implements SerializationAware {

    private static final ConverterFactory CONVERTER_FACTORY = new ChainingConverterFactory();

	private final EventProxy EventProxy;
    private final Object payloadGuard = new Object();
    // guarded by "payloadGuard"
    private final Map<Serializer, SerializedObject> serializedPayload = new HashMap<Serializer, SerializedObject>();

    public SerializedObjectHolder(EventProxy EventProxy) {
        this.EventProxy = EventProxy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> SerializedObject<T> serializePayload(Serializer serializer, Class<T> expectedRepresentation) {
        synchronized (payloadGuard) {
            SerializedObject existingForm = serializedPayload.get(serializer);
            if (existingForm == null) {
                SerializedObject<T> serialized = serializer.serialize(EventProxy.getPayload(), expectedRepresentation);
                serializedPayload.put(serializer, serialized);
                return serialized;
            } else {
                return CONVERTER_FACTORY.getConverter(existingForm.getContentType(), expectedRepresentation)
                                        .convert(existingForm);
            }
        }
    }
}
