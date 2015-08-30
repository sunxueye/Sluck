package org.sluckframework.cqrs.upcasting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.serializer.SerializedAggregateEventData;
import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.common.serializer.UnknownSerializedTypeException;
import org.sluckframework.domain.event.aggregate.AggregateEvent;

/**
 * upcast工具 用于 upcast指定 source to target 
 * 
 * @author sunxy
 * @time 2015年8月31日 上午12:26:48
 * @since 1.0
 */
public class UpcastUtils {
	

    private static final Logger logger = LoggerFactory.getLogger(UpcastUtils.class);

    private UpcastUtils() {
    }

    /**
     * Upcasts and deserializes the given <code>entry</code> using the given <code>serializer</code> and
     * <code>upcasterChain</code>.
     * 
     * @param entry               the entry containing the data of the serialized event
     * @param aggregateIdentifier the original aggregate identifier to use 
     * @param serializer          the serializer to deserialize the event with
     * @param upcasterChain       the chain containing the upcasters to upcast the events with
     * @param skipUnknownTypes    whether unknown serialized types should be ignored
     * @return a list of upcast and deserialized events
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<AggregateEvent> upcastAndDeserialize(SerializedAggregateEventData entry,
                                                                Object aggregateIdentifier,
                                                                Serializer serializer, UpcasterChain upcasterChain,
                                                                boolean skipUnknownTypes) {
        SerializedAggregateEventUpcastingContext context = new SerializedAggregateEventUpcastingContext(entry, serializer);
        List<SerializedObject> objects = upcasterChain.upcast(entry.getPayload(), context);
        List<AggregateEvent> events = new ArrayList<AggregateEvent>(objects.size());
        for (SerializedObject object : objects) {
            try {
                AggregateEvent<Object> message = new SerializedAggregateEvent<Object>(
                        new UpcastSerializedAggregateEventData(entry,
                                                            firstNonNull(aggregateIdentifier,
                                                                         entry.getAggregateIdentifier()), object),
                        serializer);
                events.add(message);
            } catch (UnknownSerializedTypeException e) {
                if (!skipUnknownTypes) {
                    throw e;
                }
                logger.info("Ignoring event of unknown type {} (rev. {}), as it cannot be resolved to a Class",
                            object.getType().getName(), object.getType().getRevision());
            }
        }
        return events;
    }

    private static Object firstNonNull(Object... instances) {
        for (Object instance : instances) {
            if (instance != null) {
                return instance;
            }
        }
        return null;
    }

}