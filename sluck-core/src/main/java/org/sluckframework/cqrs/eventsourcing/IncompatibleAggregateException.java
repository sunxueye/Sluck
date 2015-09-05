package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.common.exception.SluckNonTransientException;

/**
 * 前后矛盾的异常
 * 
 * @author sunxy
 * @time 2015年9月5日 下午11:23:56
 * @since 1.0
 */
public class IncompatibleAggregateException extends SluckNonTransientException {

	private static final long serialVersionUID = 6721947146181454974L;
	
	public IncompatibleAggregateException(String message, Exception cause) {
        super(message, cause);
    }

    public IncompatibleAggregateException(String message) {
        super(message);
    }

}
