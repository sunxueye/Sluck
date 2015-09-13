package org.sluckframework.cqrs.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.sluckframework.common.util.ReflectionUtils.methodsOf;

/**
 * 使用set方法的资源注入器,saga的资源注入方法需要set,如果set方法的第一个参数类型,和给定的类型匹配,
 * 则使用此方法注入指定资源
 *
 * Author: sunxy
 * Created: 2015-09-13 15:04
 * Since: 1.0
 */
public class SimpleResourceInjector implements ResourceInjector {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceInjector.class);
    private final Iterable<?> resources;

    /**
     * 使用给定的资源对象初始化
     *
     * @param resources The resources to inject
     */
    public SimpleResourceInjector(Object... resources) {
        this(Arrays.asList(resources));
    }

    /**
     * 使用给定的资源集合初始化
     *
     * @param resources The resources to inject
     */
    public SimpleResourceInjector(Collection<?> resources) {
        this.resources = new ArrayList<Object>(resources);
    }

    @Override
    public void injectResources(Saga saga) {
        for (Method method : methodsOf(saga.getClass())) {
            if (isSetter(method)) {
                Class<?> requiredType = method.getParameterTypes()[0];
                for (Object resource : resources) {
                    if (requiredType.isInstance(resource)) {
                        injectResource(saga, method, resource);
                    }
                }
            }
        }
    }

    private void injectResource(Saga saga, Method setterMethod, Object resource) {
        try {
            ReflectionUtils.ensureAccessible(setterMethod);
            setterMethod.invoke(saga, resource);
        } catch (IllegalAccessException e) {
            logger.warn("Unable to inject resource. Exception while invoking setter: ", e);
        } catch (InvocationTargetException e) {
            logger.warn("Unable to inject resource. Exception while invoking setter: ", e.getCause());
        }
    }

    private boolean isSetter(Method method) {
        return method.getParameterTypes().length == 1 && method.getName().startsWith("set");
    }
}
