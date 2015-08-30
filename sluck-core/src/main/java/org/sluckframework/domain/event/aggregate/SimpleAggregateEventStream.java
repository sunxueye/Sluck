package org.sluckframework.domain.event.aggregate;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * 聚合事件流的简单实现
 * 
 * @author sunxy
 * @time 2015年8月28日 下午5:11:27	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class SimpleAggregateEventStream implements AggregateEventStream {

	private static final AggregateEventStream EMPTY_STREAM = new SimpleAggregateEventStream();

    private int nextIndex;
    
	private final AggregateEvent[] events;

	public SimpleAggregateEventStream(Collection<? extends AggregateEvent> events) {
        this(events.toArray(new AggregateEvent[events.size()]));
    }

	public SimpleAggregateEventStream(AggregateEvent... events) {
        this.events = Arrays.copyOfRange(events, 0, events.length);
    }

    @Override
    public boolean hasNext() {
        return events.length > nextIndex;
    }

	@Override
    public AggregateEvent next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Trying to peek beyond the limits of this stream.");
        }
        return events[nextIndex++];
    }

	@Override
    public AggregateEvent peek() {
        if (!hasNext()) {
            throw new NoSuchElementException("Trying to peek beyond the limits of this stream.");
        }
        return events[nextIndex];
    }

    /**
     * 创建一个空的聚合流
     *
     * @return empty DomainEventStream
     */
    public static AggregateEventStream emptyStream() {
        return EMPTY_STREAM;
    }

}
