package org.sluckframework.common.property;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * 抽象的属性操作策略的实现,用户可以使用ServiceLoader策略来自定义属性加载策略
 * 在 <code>META-INF/services</code> 文件夹下,放置<code>org.sluckframework.common.property.PropertyAccessStrategy</code>
 * 自定义的实现必须有无参构造函数,而且需要实现此类
 * Author: sunxy
 * Created: 2015-09-09 23:56
 * Since: 1.0
 */
public abstract class PropertyAccessStrategy implements Comparable<PropertyAccessStrategy> {

    private static final ServiceLoader<PropertyAccessStrategy> LOADER =
            ServiceLoader.load(PropertyAccessStrategy.class);

    private static final SortedSet<PropertyAccessStrategy> STRATEGIES = new ConcurrentSkipListSet<>();

    static {
        for (PropertyAccessStrategy factory : LOADER) {
            STRATEGIES.add(factory);
        }
    }

    /**
     * 注册指定的属性获取策略
     *
     *
     * @param strategy implementation to register
     */
    public static void register(PropertyAccessStrategy strategy) {
        STRATEGIES.add(strategy);
    }

    /**
     * 取消注册的属性获取策略
     *
     * @param strategy The strategy instance to unregister
     */
    public static void unregister(PropertyAccessStrategy strategy) {
        STRATEGIES.remove(strategy);
    }

    /**
     * 使用注册的属性获取策略 来获取目标属性,如果有多个,则使用第一个(优先级最高的)
     *
     * @param targetClass  class that contains property
     * @param propertyName name of the property to create propertyReader for
     * @return suitable {@link Property}, or <code>null</code> if none is found
     */
    public static <T> Property<T> getProperty(Class<T> targetClass, String propertyName) {
        Property<T> property = null;
        Iterator<PropertyAccessStrategy> strategies = STRATEGIES.iterator();
        while (property == null && strategies.hasNext()) {
            property = strategies.next().propertyFor(targetClass, propertyName);
        }

        return property;
    }

    @Override
    public final int compareTo(PropertyAccessStrategy o) {
        if (o == this) {
            return 0;
        }
        final int diff = o.getPriority() - getPriority();
        if (diff == 0) {
            // we don't want equality...
            return getClass().getName().compareTo(o.getClass().getName());
        }
        return diff;
    }

    /**
     * 获取策略的优先级
     *
     * @return a value reflecting relative priority, <code>Integer.MAX_VALUE</code> being evaluated first
     */
    protected abstract int getPriority();

    /**
     * 从指定的目标类中获获取指定属性
     *
     * @param targetClass The class on which to find the property
     * @param property    The name of the property to find
     * @param <T>         The type of class on which to find the property
     * @return the Property instance providing access to the property value, or <code>null</code> if property could not
     * be found.
     */
    protected abstract <T> Property<T> propertyFor(Class<T> targetClass, String property);
}


