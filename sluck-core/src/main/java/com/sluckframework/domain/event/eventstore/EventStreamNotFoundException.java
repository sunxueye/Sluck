package com.sluckframework.domain.event.eventstore;

import com.sluckframework.domain.identifier.Identifier;

/**
 * 在eventStore中找不到给定的聚合标示符对应的聚合
 * 
 * @author sunxy
 * @time 2015年8月29日 上午1:48:48
 * @since 1.0
 */
public class EventStreamNotFoundException extends EventStoreException {

	private static final long serialVersionUID = 5611966686596063336L;
	
	public EventStreamNotFoundException(String message) {
		super(message);
	}
	
	public EventStreamNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
    public EventStreamNotFoundException(String type, Identifier<?> identifier) {
        this(String.format("Aggregate of type [%s] with identifier [%s] cannot be found.",
                           type, identifier));
    }

}
