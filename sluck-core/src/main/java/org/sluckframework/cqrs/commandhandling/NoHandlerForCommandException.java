package org.sluckframework.cqrs.commandhandling;

import org.sluckframework.common.exception.SluckNonTransientException;

/**
 * 武此处理器
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:16:38	
 * @since 1.0
 */
public class NoHandlerForCommandException extends SluckNonTransientException {

	private static final long serialVersionUID = 3341980702809077663L;
	
	public NoHandlerForCommandException(String message) {
	     super(message);
	}

}
