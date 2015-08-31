package org.sluckframework.common.serializer;

import org.joda.time.DateTime;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 在 upcasting 一个 payload 后 ，保留原始的 SerializedDomainEventData 信息
 * 
 * @author sunxy
 * @time 2015年8月31日 上午10:38:10	
 * @since 1.0
 */
public class UpcastSerializedAggregateEventData<T> implements SerializedAggregateEventData<T> {

    private final SerializedAggregateEventData<T> original;
    private final Identifier<?> identifier;
    private final SerializedObject<T> upcastPayload;

    /**
     * 使用 指定 信息初始化
     *
     * @param original            The original SerializedDomainEventData instance
     * @param aggregateIdentifier The aggregate identifier instance
     * @param upcastPayload       The replacement payload
     */
    public UpcastSerializedAggregateEventData(SerializedAggregateEventData<T> original, Identifier<?> aggregateIdentifier,
                                           SerializedObject<T> upcastPayload) {
        this.original = original;
        this.identifier = aggregateIdentifier;
        this.upcastPayload = upcastPayload;
    }

    @Override
    public Object getEventIdentifier() {
        return original.getEventIdentifier();
    }

    @Override
    public Identifier<?> getAggregateIdentifier() {
        return identifier;
    }

    @Override
    public long getSequenceNumber() {
        return original.getSequenceNumber();
    }

    @Override
    public DateTime getTimestamp() {
        return original.getTimestamp();
    }


    @Override
    public SerializedObject<T> getPayload() {
        return upcastPayload;
    }
}
