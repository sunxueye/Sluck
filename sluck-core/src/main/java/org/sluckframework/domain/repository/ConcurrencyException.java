package org.sluckframework.domain.repository;

import org.sluckframework.common.exception.SluckTransientException;

/**
 * 同步异常，可能由于两个线程 同步修改相同的聚合
 * 
 * @author sunxy
 * @time 2015年8月31日 上午12:21:23
 * @since 1.0
 */
public class ConcurrencyException extends SluckTransientException {

	private static final long serialVersionUID = -4467802568761880929L;
	
	public ConcurrencyException(String message) {
        super(message);
    }

    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }

}
