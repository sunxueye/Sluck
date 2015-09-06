package org.sluckframework.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 监视器的注册类
 * 
 * @author sunxy
 * @time 2015年9月6日 下午8:58:27
 * @since 1.0
 */
public abstract class MonitorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(MonitorRegistry.class);
    private static final List<MonitorRegistry> registries;

    static {
        registries = new ArrayList<MonitorRegistry>();
        ServiceLoader<MonitorRegistry> registryLoader = ServiceLoader.load(MonitorRegistry.class);
        for (MonitorRegistry monitorRegistry : registryLoader) {
            registries.add(monitorRegistry);
        }
    }

    /**
     * 注册给定的 监视器 bean
     *
     * @param monitoringBean The bean containing the monitoring information
     * @param componentType  The type of component that the monitoring bean provides information for
     */
    public static void registerMonitoringBean(Object monitoringBean, Class<?> componentType) {
        for (MonitorRegistry registry : registries) {
            try {
                registry.registerBean(monitoringBean, componentType);
            } catch (Exception e) {
                logger.warn("Exception when registering {} with {} ", monitoringBean, registry, e);
            } catch (Error e) {
                logger.warn("Error when registering {} with {} ", monitoringBean, registry, e);
            }
        }
    }

    /**
     * 注册 自定义的 监视器 bean
     *
     * @param monitoringBean The bean to register
     * @param componentType  The type of component that the monitoring bean provides information for
     */
    protected abstract void registerBean(Object monitoringBean, Class<?> componentType);

}
