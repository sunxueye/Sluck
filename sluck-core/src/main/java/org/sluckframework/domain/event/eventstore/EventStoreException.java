package org.sluckframework.domain.event.eventstore;

import org.sluckframework.common.exception.SluckNonTransientException;

/**
 * 事件存储异常
 * 
 * @author sunxy
 * @time 2015年8月29日 上午1:43:23
 * @since 1.0
 */
public class EventStoreException extends SluckNonTransientException {
	
	private static final long serialVersionUID = 5295507771948166175L;

	public EventStoreException(String message) {
		super(message);
	}

	public EventStoreException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
