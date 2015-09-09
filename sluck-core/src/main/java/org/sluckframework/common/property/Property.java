package org.sluckframework.common.property;

/**
 * 从目标对象中读取属性值
 *
 * Author: sunxy
 * Created: 2015-09-09 23:53
 * Since: 1.0
 */
public interface Property<T> {

    /**
     * 返回目标对象中的 属性值
     *
     * @param target The instance to get the property value from
     * @return the property value on <code>target</code>
     */
    <V> V getValue(T target);
}
