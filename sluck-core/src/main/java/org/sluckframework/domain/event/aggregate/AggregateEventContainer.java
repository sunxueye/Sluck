package org.sluckframework.domain.event.aggregate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sluckframework.common.exception.Assert;
import org.sluckframework.domain.aggregate.EventRegistrationCallback;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 事件容器，用于保存 聚合产生的 事件
 * 
 * @author sunxy
 * @time 2015年8月28日 下午3:03:33
 * @since 1.0
 */
public class AggregateEventContainer implements Serializable {

	private static final long serialVersionUID = 7451332702283625191L;

	@SuppressWarnings("rawtypes")
	private final List<AggregateEvent> events = new ArrayList<AggregateEvent>();
	
	private final Identifier<?> aggregateIdentifier;
	
	private Long lastCommittedSequenceNumber;
	private transient Long lastSequenceNumber; 
	
	private transient List<EventRegistrationCallback> registrationCallbacks; 

	/**
	 * 为指定的聚合初始聚合事件容器
	 * @param aggregateIdentifier
	 */
	public AggregateEventContainer(Identifier<?> aggregateIdentifier) {
		this.aggregateIdentifier = aggregateIdentifier;
	}

	/**
	 * 增加事件到容器中，需要注意事件的顺序
	 * @param event the event to add to this container
	 * @return the AggregateEvent added to the container
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public AggregateEvent addEvent(AggregateEvent aggregateEvent) {
		if (aggregateEvent.getAggregateIdentifier() == null) {
			aggregateEvent = new GenericAggregateEvent(aggregateEvent.getIdentifier(),
					                                   aggregateEvent.occurredOn(),
                                                       aggregateIdentifier,
                                                       aggregateEvent.getSequenceNumber(),
                                                       aggregateEvent.getPayload());
		}
		if (registrationCallbacks != null) {
			for (EventRegistrationCallback callback : registrationCallbacks) {
				aggregateEvent = callback.onRegisteredEvent(aggregateEvent);
			}
		}
		lastSequenceNumber = aggregateEvent.getSequenceNumber();
		events.add(aggregateEvent);
		return aggregateEvent;
	}

	/**
	 * 获取事件容器中没有提交事件的事件流，返回的事件流都是新建的
	 * 
	 * @return a DomainEventStream providing access to the events in this
	 *         container
	 */
	public AggregateEventStream getEventStream() {
		return new SimpleAggregateEventStream(events);
	}

	public Identifier<?> getAggregateIdentifier() {
		return aggregateIdentifier;
	}

	/**
	 * 初始化容器的 lastCommittedSequenceNumber ,容器的必须是未初始化的
	 * @param lastKnownSequenceNumber
	 */
	public void initializeSequenceNumber(Long lastKnownSequenceNumber) {
		Assert.state(events.size() == 0,
				"Cannot set first sequence number if events have already been added");
		lastCommittedSequenceNumber = lastKnownSequenceNumber;
	}

	/**
	 * Returns the sequence number of the event last added to this container.
	 *
	 * @return the sequence number of the last event
	 */
	public Long getLastSequenceNumber() {
		if (events.isEmpty()) {
			return lastCommittedSequenceNumber;
		} else if (lastSequenceNumber == null) {
			lastSequenceNumber = events.get(events.size() - 1).getSequenceNumber();
		}
		return lastSequenceNumber;
	}

	/**
	 * 返回最后提交事件的sequence number, 如果事件没有提交，则返回null
	 *
	 * @return the sequence number of the last committed event
	 */
	public Long getLastCommittedSequenceNumber() {
		return lastCommittedSequenceNumber;
	}

	/**
	 * 容器提交:清空容器和 callback
	 */
	public void commit() {
		lastCommittedSequenceNumber = getLastSequenceNumber();
		events.clear();
		if (registrationCallbacks != null) {
			registrationCallbacks.clear();
		}
	}

	public int size() {
		return events.size();
	}

	/**
	 * 返回容器中的事件，这个返回集合是不可修改的
	 * @return a list containing the events in this container
	 */
	@SuppressWarnings("rawtypes")
	public List<AggregateEvent> getEventList() {
		return Collections.unmodifiableList(events);
	}

	/**
	 * 增加 eventRegistrationCallback 到容器中
	 *
	 * @param eventRegistrationCallback
	 *            The callback to notify when an Event is registered.
	 */
	public void addEventRegistrationCallback(
			EventRegistrationCallback eventRegistrationCallback) {
		if (registrationCallbacks == null) {
			this.registrationCallbacks = new ArrayList<EventRegistrationCallback>();
		}
		this.registrationCallbacks.add(eventRegistrationCallback);
		for (int i = 0; i < events.size(); i++) {
			events.set(i, eventRegistrationCallback.onRegisteredEvent(events.get(i)));
		}
	}

}
