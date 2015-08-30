package org.sluckframework.domain.event.aggregate;

import java.util.UUID;

import org.joda.time.DateTime;
import org.sluckframework.domain.event.EventProxy;

import static org.sluckframework.domain.identifier.IdentifierValidator.validateIdentifier;

/**
 * 通用的eventProxy的实现
 * 
 * @author sunxy
 * @time 2015年8月29日 下午3:17:20
 * @since 1.0
 */
public class GenericEvent<T> implements EventProxy<T> {

	private static final long serialVersionUID = 3788365038915270943L;
	
	private final Object identifier;
    private final Class<T> payloadType;
    private final T payload;
    private final DateTime dataTime;
    
	/**
	 * 默认使用UUID作为 事件标示符
	 * @param payload
	 */
	@SuppressWarnings("unchecked")
	public GenericEvent(T payload) {
		this(UUID.randomUUID().toString(), payload, (Class<T>) payload.getClass(), new DateTime());
	}
	
	@SuppressWarnings("unchecked")
	public GenericEvent(Object identifier, T payload) {
		this(identifier, payload, (Class<T>) payload.getClass(), new DateTime());
	}
	
	@SuppressWarnings("unchecked")
	public GenericEvent(Object identifier, T payload, DateTime time) {
		this(identifier, payload, (Class<T>) payload.getClass(), time);
	}
	
	public GenericEvent(Object identifier, T payload, Class<T> payloadType, DateTime time) {
		validateIdentifier(identifier.getClass());
		this.identifier = identifier;
		this.payloadType = payloadType;
		this.payload = payload;
		this.dataTime = time;
	}
	
	@Override
	public Object getIdentifier() {
		return identifier;
	}

	@Override
	public T getPayload() {
		return payload;
	}

	@Override
	public Class<T> getPayloadType() {
		return payloadType;
	}

	@Override
	public DateTime occurredOn() {
		return dataTime;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		GenericEvent other = (GenericEvent) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return  String.format("GenericEvent[%s]", getPayloadType().getName() + ", id:" + identifier);
	}

}
