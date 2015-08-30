package org.sluckframework.common.serializer;

import org.sluckframework.common.exception.SluckNonTransientException;

/**
 * serializate 异常
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:40:52
 * @since 1.0
 */
public class SerializationException extends SluckNonTransientException {

	private static final long serialVersionUID = 1378799826304781322L;

	public SerializationException(String message) {
		super(message);
	}
	
	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
