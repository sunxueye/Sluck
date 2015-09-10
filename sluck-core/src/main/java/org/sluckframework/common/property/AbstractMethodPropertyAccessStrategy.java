package org.sluckframework.common.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 属性操作策略的抽象实现,根据方法名称获取方法,然后执行 需要 no-arg public
 *
 * Author: sunxy
 * Created: 2015-09-10 23:21
 * Since: 1.0
 */
public abstract class AbstractMethodPropertyAccessStrategy extends PropertyAccessStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BeanPropertyAccessStrategy.class);

    @Override
    public <T> Property<T> propertyFor(Class<T> targetClass, String property) {
        String methodName = getterName(property);
        try {
            final Method method = targetClass.getMethod(methodName);
            if (!Void.TYPE.equals(method.getReturnType())) {
                return new MethodAccessedProperty<T>(method, property);
            }
            logger.debug(
                    "Method with name '{}' in '{}' cannot be accepted as a property accessor, as it returns void",
                    methodName, targetClass.getName());
        } catch (NoSuchMethodException e) {
            logger.debug("No method with name '{}' found in {}", methodName, targetClass.getName(), e);
        }
        return null;
    }

    /**
     * 返回方法名称
     *
     * @param property The property to access
     * @return the name of the method use as accessor
     */
    protected abstract String getterName(String property);
}
