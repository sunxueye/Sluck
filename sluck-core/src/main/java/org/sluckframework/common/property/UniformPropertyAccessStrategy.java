package org.sluckframework.common.property;

/**
 * 返回相同的属性名称策略
 *
 * Author: sunxy
 * Created: 2015-09-10 23:19
 * Since: 1.0
 */
public class UniformPropertyAccessStrategy extends AbstractMethodPropertyAccessStrategy {

    @Override
    protected String getterName(String property) {
        return property;
    }

    @Override
    protected int getPriority() {
        return -1024;
    }
}

