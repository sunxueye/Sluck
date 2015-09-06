package org.sluckframework.common.annotation;

import org.sluckframework.common.exception.SluckException;

/**
 * 方法执行异常
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:38:05	
 * @since 1.0
 */
public class MessageHandlerInvocationException extends SluckException{

	private static final long serialVersionUID = 8144663654716785339L;
	
	public MessageHandlerInvocationException(String message, Throwable cause) {
	     super(message, cause);
	}

}
