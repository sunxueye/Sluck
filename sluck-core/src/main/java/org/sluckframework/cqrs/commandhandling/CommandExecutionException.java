package org.sluckframework.cqrs.commandhandling;

import org.sluckframework.common.exception.SluckException;

/**
 * 在处理 命令的 过程中 抛出异常
 * 
 * @author sunxy
 * @time 2015年9月7日 上午11:59:36	
 * @since 1.0
 */
public class CommandExecutionException extends SluckException {

	private static final long serialVersionUID = -2958786077537146965L;
	
	public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
