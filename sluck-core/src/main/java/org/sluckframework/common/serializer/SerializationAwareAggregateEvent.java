package org.sluckframework.common.serializer;


import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.identifier.Identifier;

/**
 * @author sunxy
 * @time 2015年9月7日 下午11:35:12
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class SerializationAwareAggregateEvent<T, AID extends Identifier<?>> extends
		SerializationAwareEventProxy<T> implements AggregateEvent<T, AID> {

	private static final long serialVersionUID = 3604150564332415524L;
    private final AggregateEvent<T,AID> AggregateEvent;

    /**
     * 将持有的 的指定聚合事件 变为 可序列化的 聚合事件
     *
     * @param message The message to wrap
     * @param <T>     The payload type of the message
     * @return a serialization aware version of the given message
     */
    @SuppressWarnings("rawtypes")
	public static <T, AID extends Identifier<?>> AggregateEvent wrap(AggregateEvent<T, AID> message) {
        if (message instanceof SerializationAware) {
            return message;
        }
        return new SerializationAwareAggregateEvent<T, AID>(message);
    }

    protected SerializationAwareAggregateEvent(AggregateEvent<T, AID> message) {
        super(message);
        this.AggregateEvent = message;
    }

    @Override
    public long getSequenceNumber() {
        return AggregateEvent.getSequenceNumber();
    }

    @Override
    public AID getAggregateIdentifier() {
        return AggregateEvent.getAggregateIdentifier();
    }

    /**
     * Replacement function for Java Serialization API. When this object is serialized, it is replaced by the
     * implementation it wraps.
     *
     * @return the AggregateEvent wrapped by this message
     */
    @Override
    protected Object writeReplace() {
        return AggregateEvent;
    }

}
