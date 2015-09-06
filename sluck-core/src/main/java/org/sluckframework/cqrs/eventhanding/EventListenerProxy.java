package org.sluckframework.cqrs.eventhanding;
/**
 * eventListener动态代理
 * 
 * @author sunxy
 * @time 2015年9月6日 下午9:01:46
 * @since 1.0
 */
public interface EventListenerProxy extends EventListener{
	
	/**
	 * 返回代理处理的 event类型
     *
     * @return the instance type that this proxy delegates all event handling to
     */
    Class<?> getTargetType();

}
