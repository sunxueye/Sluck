package com.sluckframework.domain.aggregate;

import java.util.concurrent.atomic.AtomicLong;

import com.sluckframework.domain.event.aggregate.AggregateEvent;
import com.sluckframework.domain.event.aggregate.AggregateEventContainer;
import com.sluckframework.domain.event.aggregate.AggregateEventStream;
import com.sluckframework.domain.event.aggregate.SimpleAggregateEventStream;
import com.sluckframework.domain.identifier.Identifier;

/**
 * 聚合根的抽象实现，所有聚合根的基类
 * 
 * @author sunxy
 * @time 2015年8月28日 下午2:52:17	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractAggregateRoot<ID extends Identifier<?>> implements AggregateRoot<ID> {
	
	private static final long serialVersionUID = 2736056672727780021L;
	
	private volatile AggregateEventContainer eventContainer;

    private boolean deleted = false;

    private Long lastEventSequenceNumber;

    private AtomicLong version = new AtomicLong(0);

    /**
     * 注册聚合事件, 会执行 EventRegistrationCallback
     *
     * @return The Event added to container
     */
    
	protected AggregateEvent registerEvent(AggregateEvent event) {
        return getEventContainer().addEvent(event);
    }

    protected void markDeleted() {
        this.deleted = true;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void addEventRegistrationCallback(EventRegistrationCallback eventRegistrationCallback) {
        getEventContainer().addEventRegistrationCallback(eventRegistrationCallback);
    }

    @Override
    public AggregateEventStream getUncommittedEvents() {
        if (eventContainer == null) {
            return SimpleAggregateEventStream.emptyStream();
        }
        return eventContainer.getEventStream();
    }

    @Override
    public void commitEvents() {
        if (eventContainer != null) {
            lastEventSequenceNumber = eventContainer.getLastSequenceNumber();
            eventContainer.commit();
        }
    }

    @Override
    public int getUncommittedEventCount() {
        return eventContainer != null ? eventContainer.size() : 0;
    }

    /**
     * 初始化事件流
     *
     * @param lastSequenceNumber The sequence number of the last event from this aggregate
     */
    protected void initializeEventStream(long lastSequenceNumber) {
        getEventContainer().initializeSequenceNumber(lastSequenceNumber);
        lastEventSequenceNumber = lastSequenceNumber >= 0 ? lastSequenceNumber : null;
    }

    /**
     * 返回最后提交事件的sequence number, 如果没有则为 null
     *
     * @return the sequence number of the last committed event
     */
    protected Long getLastCommittedEventSequenceNumber() {
        if (eventContainer == null) {
            return lastEventSequenceNumber;
        }
        return eventContainer.getLastCommittedSequenceNumber();
    }

    @Override
    public Long getVersion() {
        return version.get();
    }
    
    private AggregateEventContainer getEventContainer() {
        if (eventContainer == null) {
        	Identifier identifier = getIdentifier();
            if (identifier == null) {
                throw new AggregateIdentifierNotInitializedException(
                        "AggregateIdentifier is unknown in [" + getClass().getName() + "]. "
                                + "Make sure the Aggregate Identifier is initialized before registering events.");
            }
            eventContainer = new AggregateEventContainer(identifier);
            eventContainer.initializeSequenceNumber(lastEventSequenceNumber);
        }
        return eventContainer;
    }

}
