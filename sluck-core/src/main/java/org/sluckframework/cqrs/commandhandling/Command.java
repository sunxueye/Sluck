package org.sluckframework.cqrs.commandhandling;

import org.sluckframework.domain.event.EventProxy;


/**
 * 命令 接口
 * 
 * @author sunxy
 * @time 2015年9月6日 下午8:16:50
 * @since 1.0
 */
public interface Command<T> extends EventProxy<T> {
	
	/**
	 * 命令的名称
     *
     * @return the name of the command
     */
    String getCommandName();

}
