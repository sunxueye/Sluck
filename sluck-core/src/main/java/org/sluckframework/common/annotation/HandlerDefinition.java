package org.sluckframework.common.annotation;

import java.lang.reflect.AccessibleObject;

/**
 * 处理器定义
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:28:23	
 * @since 1.0
 */
public interface HandlerDefinition<T extends AccessibleObject> {

    /**
     * 判断给定的 member 是否为 messagehandler
     *
     * @param member The member to verify
     * @return <code>true</code> if the given <code>member</code> is a message handler, otherwise <code>false</code>
     */
    boolean isMessageHandler(T member);

    /**
     * 在指定的 member中 解析出 payload 的类型
     *
     * @param member the member method
     * @return the explicitly configured payload type, or <code>null</code> if the payload must be deducted from the
     * handler's parameters
     */
    Class<?> resolvePayloadFor(T member);

}
