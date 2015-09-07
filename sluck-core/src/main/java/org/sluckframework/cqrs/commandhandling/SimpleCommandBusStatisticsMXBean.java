package org.sluckframework.cqrs.commandhandling;

import java.util.List;

/**
 * simple cb 监视器
 * 
 * @author sunxy
 * @time 2015年9月7日 上午9:50:39	
 * @since 1.0
 */
public interface SimpleCommandBusStatisticsMXBean {

    /**
     * 命令处理器数量
     *
     * @return long representing the amount of handlers
     */
    long getCommandHandlerCount();

    /**
     * 命令处理器的名称
     *
     * @return List of strings representing the names of registered handlers
     */
    List<String> getHandlerTypes();

    /**
     * 已接受到命令的数量
     *
     * @return long representing the amount of commands received
     */
    long getReceivedCommandCount();

    /**
     * 重置接收到命令的数量
     */
    void resetReceivedCommandsCounter();

}
