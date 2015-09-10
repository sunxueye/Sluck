package org.sluckframework.common.property;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.String.format;
import static org.sluckframework.common.util.ReflectionUtils.ensureAccessible;

/**
 * 方法属性,里面包含了目标类型的指定方法
 *
 * Author: sunxy
 * Created: 2015-09-10 23:26
 * Since: 1.0
 */
public class MethodAccessedProperty<T> implements Property<T> {

    private final Method method;
    private final String property;

    /**
     * 使用指定的方法和方法名初始化
     *
     * @param accessorMethod The method providing the property value
     * @param propertyName   The name of the property
     */
    public MethodAccessedProperty(Method accessorMethod, String propertyName) {
        property = propertyName;
        method = ensureAccessible(accessorMethod);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getValue(T target) {
        try {
            return (V) method.invoke(target);
        } catch (IllegalAccessException e) {
            throw new PropertyAccessException(format(
                    "Failed to get value of '%s' using method '%s()' of '%s'. Property methods should be accessible",
                    property, method.getName(), target.getClass().getName()), e);
        } catch (InvocationTargetException e) {
            throw new PropertyAccessException(format(
                    "Failed to get value of '%s' using method '%s()' of '%s'. "
                            + "Property methods should not throw exceptions.",
                    property, method.getName(), target.getClass().getName()), e);
        }
    }
}
