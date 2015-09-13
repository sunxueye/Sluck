package org.sluckframework.cqrs.saga;

/**
 * saga创建策略
 *
 * Author: sunxy
 * Created: 2015-09-13 14:38
 * Since: 1.0
 */
public enum SagaCreationPolicy {

    /**
     * 从不创建新的实例,即使不存在
     */
    NONE,

    /**
     * 只有不存在的时候再创建实例
     */
    IF_NONE_FOUND,

    /**
     * 总是创建新的实例
     */
    ALWAYS
}
