package com.sluckframework.common.exception;
/**
 * @author sunxy
 * @time 2015年8月28日 下午2:47:37	
 * @since 1.0
 */
public class SluckConfigurationException extends SluckNonTransientException {

	private static final long serialVersionUID = 8278198181650520212L;

	public SluckConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SluckConfigurationException(String message) {
		super(message);
	}

}
