package org.sluckframework.common.serializer;

import org.joda.time.DateTime;
import org.sluckframework.domain.event.EventProxy;

/**
 * @author sunxy
 * @time 2015年9月7日 下午11:26:58
 * @since 1.0
 */
public class SerializationAwareEventProxy<T> implements SerializationAware, EventProxy<T> {

    private static final long serialVersionUID = 4760330133615704145L;

    private final EventProxy<T> EventProxy;
    private final SerializedObjectHolder serializedObjectHolder;

    /**
     * 持有给定的 事件 返回一个 SerializationAware 消息
     *
     * @param message The message to wrap
     * @param <T>     The payload type of the message
     * @return a serialization aware version of the given message
     */
    public static <T> EventProxy<T> wrap(EventProxy<T> message) {
        if (message instanceof SerializationAware) {
            return message;
        }
        return new SerializationAwareEventProxy<>(message);
    }

    /**
     * 使用给定的信息初始化
     * 
     * @param EventProxy The message to wrap
     */
    protected SerializationAwareEventProxy(EventProxy<T> EventProxy) {
        this.EventProxy = EventProxy;
        this.serializedObjectHolder = new SerializedObjectHolder(EventProxy);
    }

    @Override
    public Object getIdentifier() {
        return EventProxy.getIdentifier();
    }


    @Override
    public T getPayload() {
        return EventProxy.getPayload();
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public Class getPayloadType() {
        return EventProxy.getPayloadType();
    }

    @Override
    public DateTime occurredOn() {
        return EventProxy.occurredOn();
    }


    @Override
    public <R> SerializedObject<R> serializePayload(Serializer serializer,
                                                    Class<R> expectedRepresentation) {
        return serializedObjectHolder.serializePayload(serializer, expectedRepresentation);
    }


    /**
     * Replacement function for Java Serialization API. When this object is serialized, it is replaced by the
     * implementation it wraps.
     *
     * @return the EventProxy wrapped by this message
     */
    protected Object writeReplace() {
        return EventProxy;
    }
}
