package org.sluckframework.common.annotation;

import static java.util.ServiceLoader.load;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.WeakHashMap;

import org.slf4j.LoggerFactory;

/**
 * 此工厂 会 使用 ServiceLoad机制 加载 注册的 自定义 参数工厂
 * <code>META-INF/services/org.sluckframework.common.annotation.ParameterResolverFactory</code>.文件
 * 
 * @author sunxy
 * @time 2015年9月6日 下午4:23:57	
 * @since 1.0
 */
public class ClasspathParameterResolverFactory {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ClasspathParameterResolverFactory.class);
    private static final Object monitor = new Object();
    private static final Map<ClassLoader, WeakReference<ParameterResolverFactory>> FACTORIES =
            new WeakHashMap<ClassLoader, WeakReference<ParameterResolverFactory>>();

    /**
     * 根据 给定的 class 使用其 classLoader加载
     *
     * @param clazz The class for which the parameter resolver must be returned
     * @return a ClasspathParameterResolverFactory that can resolve parameters for the given class
     */
    public static ParameterResolverFactory forClass(Class<?> clazz) {
        return forClassLoader(clazz == null ? null : clazz.getClassLoader());
    }

    /**
     * ServiceLoader 机制加载
     *
     * @param classLoader The class loader to locate the implementations with
     * @return a ParameterResolverFactory instance using the given classLoader
     */
    public static ParameterResolverFactory forClassLoader(ClassLoader classLoader) {
        synchronized (monitor) {
            ParameterResolverFactory factory;
            if (!FACTORIES.containsKey(classLoader)) {
                factory = MultiParameterResolverFactory.ordered(findDelegates(classLoader));
                FACTORIES.put(classLoader, new WeakReference<ParameterResolverFactory>(factory));
                return factory;
            }
            factory = FACTORIES.get(classLoader).get();
            if (factory == null) {
                factory = MultiParameterResolverFactory.ordered(findDelegates(classLoader));
                FACTORIES.put(classLoader, new WeakReference<ParameterResolverFactory>(factory));
            }
            return factory;
        }
    }

    private static List<ParameterResolverFactory> findDelegates(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        Iterator<ParameterResolverFactory> iterator = load(ParameterResolverFactory.class, classLoader).iterator();
        //noinspection WhileLoopReplaceableByForEach
        final List<ParameterResolverFactory> factories = new ArrayList<ParameterResolverFactory>();
        while (iterator.hasNext()) {
            try {
                ParameterResolverFactory factory = iterator.next();
                factories.add(factory);
            } catch (ServiceConfigurationError e) {
                logger.info("ParameterResolverFactory instance ignored, as one of the required classes is not available"
                                    + "on the classpath: {}", e.getMessage());
            } catch (NoClassDefFoundError e) {
                logger.info("ParameterResolverFactory instance ignored. It relies on a class that cannot be found: {}", e.getMessage());
            }
        }
        return factories;
    }

}
