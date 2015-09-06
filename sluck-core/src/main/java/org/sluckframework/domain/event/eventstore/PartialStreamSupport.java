package org.sluckframework.domain.event.eventstore;

import org.sluckframework.domain.event.aggregate.AggregateEventStream;

/**
 * 支持局部 读取的流
 * 
 * @author sunxy
 * @time 2015年9月6日 下午5:26:06	
 * @since 1.0
 */
public interface PartialStreamSupport {
	
	AggregateEventStream readEvents(String type, Object identifier, long firstSequenceNumber);

	AggregateEventStream readEvents(String type, Object identifier, long firstSequenceNumber, long lastSequenceNumber);

}
