package org.sluckframework.domain.event;


/**
 * 参数解析器
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:01:16	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public interface ParameterResolver<T> {

    /**
     * 根据 eventProxy 解析参数
     *
     * @param message The message to resolve the value from
     * @return the parameter value for the handler
     */
    T resolveParameterValue(EventProxy message);

    /**
     * 是否可以解析 message的参数
     *
     * @param message The message to evaluate
     * @return <code>true</code> if this resolver can provide a value for the message, otherwise <code>false</code>
     */
	boolean matches(EventProxy message);

}
