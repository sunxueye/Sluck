package org.sluckframework.cqrs.eventsourcing;

import java.util.ArrayDeque;
import java.util.Queue;

import org.sluckframework.common.exception.Assert;
import org.sluckframework.domain.aggregate.AbstractAggregateRoot;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.aggregate.GenericAggregateEvent;
import org.sluckframework.domain.identifier.Identifier;


/**
 * @author sunxy
 * @time 2015年9月5日 下午11:16:30
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractEventSourcedAggregateRoot<ID extends Identifier<?>>
		extends AbstractAggregateRoot<ID> implements EventSourcedAggregateRoot<ID> {

	private static final long serialVersionUID = 939281876222805132L;
	
    private transient boolean inReplay = false;

    private transient boolean applyingEvents = false;
    private transient Queue<Object> eventsToApply = new ArrayDeque<Object>(); //payload

    /**
     * 使用聚合事件流，支持快照事件
     */
	@Override
    public void initializeState(AggregateEventStream AggregateEventStream) {
        Assert.state(getUncommittedEventCount() == 0, "Aggregate is already initialized");
        inReplay = true;
        long lastSequenceNumber = -1;
        while (AggregateEventStream.hasNext()) {
        	AggregateEvent event = AggregateEventStream.next();
            lastSequenceNumber = event.getSequenceNumber();
            handleRecursively(event);
        }
        initializeEventStream(lastSequenceNumber);
        inReplay = false;
    }

    /**
     * 
     * 处理事件，并 表明为未提交事件，将会被 事件存储 储存
     *
     * @param eventPayload The payload of the event to apply
     */
    @SuppressWarnings("unchecked")
	protected void apply(Object eventPayload) {
        if (inReplay) {
            return;
        }
        // ensure that nested invocations know they are nested
        boolean wasNested = applyingEvents;
        applyingEvents = true;
        try {
            if (getIdentifier() == null) {
                Assert.state(!wasNested,
                             "Applying an event in an @EventSourcingHandler is allowed, but only *after* the "
                                     + "aggregate identifier has been set");
                // workaround for aggregates that set the aggregate identifier in an Event Handler
                if (getUncommittedEventCount() > 0 || getVersion() != null) {
                    throw new IncompatibleAggregateException("The Aggregate Identifier has not been initialized. "
                                                                     + "It must be initialized at the latest when the "
                                                                     + "first event is applied.");
                }
                final GenericAggregateEvent message = new GenericAggregateEvent(null, 0, eventPayload);
                                                                                                       
                handleRecursively(message);
                registerEvent(message);
            } else {
                // eventsToApply may heb been set to null by serialization
                if (eventsToApply == null) {
                    eventsToApply = new ArrayDeque<Object>();
                }
                eventsToApply.add(eventPayload);
            }

            while (!wasNested && eventsToApply != null && !eventsToApply.isEmpty()) {
                final Object payload = eventsToApply.poll();
                handleRecursively(registerPayload(payload));
            }
        } finally {
            applyingEvents = wasNested;
        }
    }

    @Override
    public void commitEvents() {
        applyingEvents = false;
        if (eventsToApply != null) {
            eventsToApply.clear();
        }
        super.commitEvents();
    }

    /**
     * 是否在回放事件中
     *
     * @return <code>true</code> if the aggregate is live, <code>false</code> when the aggregate is relaying historic
     * events.
     */
    protected boolean isLive() {
        return !inReplay;
    }

	private void handleRecursively(AggregateEvent event) {
        handle(event);
        Iterable<? extends EventSourcedEntity> childEntities = getChildEntities();
        if (childEntities != null) {
            for (EventSourcedEntity entity : childEntities) {
                if (entity != null) {
                    entity.registerAggregateRoot(this);
                    entity.handleRecursively(event);
                }
            }
        }
    }

    /**
     * 获取聚合中的实体
     *
     * @return a list of event sourced entities contained in this aggregate
     */
    protected abstract Iterable<? extends EventSourcedEntity> getChildEntities();

    protected abstract void handle(AggregateEvent event);

    @Override
    public Long getVersion() {
        return getLastCommittedEventSequenceNumber();
    }

}
