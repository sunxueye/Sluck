package com.sluckframework.common.exception;
/**
 * 表示瞬态的异常，可以被重试操作解决的异常
 * 
 * @author sunxy
 * @time 2015年8月28日 下午2:37:31	
 * @since 1.0
 */
public class SluckTransientException extends SluckException {

	private static final long serialVersionUID = 5834592698305687393L;
	
	public SluckTransientException(String message, Throwable cause) {
		super(message, cause);
	}

	public SluckTransientException(String message) {
		super(message);
	}

}
