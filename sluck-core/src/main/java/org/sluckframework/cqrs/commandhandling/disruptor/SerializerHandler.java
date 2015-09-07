package org.sluckframework.cqrs.commandhandling.disruptor;


import org.sluckframework.common.serializer.SerializationAware;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.domain.event.EventProxy;

import com.lmax.disruptor.EventHandler;

/**
 * serializer的处理器，处理 序列化的 命令
 * 
 * @author sunxy
 * @time 2015年9月7日 下午11:04:38
 * @since 1.0
 */
public class SerializerHandler implements EventHandler<CommandHandlingEntry> {

    private final Serializer serializer;
    private final int serializerId;
    private final Class<?> serializedRepresentation;

    /**
     * 使用给定的属性初始化
     *
     * @param serializer               The serializer to pre-serialize with
     * @param serializerSegmentId      The segment of this instance to handle
     * @param serializedRepresentation The representation to which to serialize the payload and meta data
     */
    public SerializerHandler(Serializer serializer, int serializerSegmentId, Class<?> serializedRepresentation) {
        this.serializer = serializer;
        this.serializerId = serializerSegmentId;
        this.serializedRepresentation = serializedRepresentation;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void onEvent(CommandHandlingEntry event, long sequence, boolean endOfBatch) throws Exception {
        if (event.getSerializerSegmentId() == serializerId) {
            for (EventProxy eventMessage : event.getUnitOfWork().getEventsToPublish()) {
                if (eventMessage instanceof SerializationAware) {
                    ((SerializationAware) eventMessage).serializePayload(serializer, serializedRepresentation);
                }
            }
        }
    }
}
