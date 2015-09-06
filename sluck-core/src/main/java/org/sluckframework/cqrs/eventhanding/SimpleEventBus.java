package org.sluckframework.cqrs.eventhanding;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.monitoring.MonitorRegistry;


/**
 * event bus简单实现， 同步返回 event handler处理结果
 * 
 * @author sunxy
 * @time 2015年9月6日 下午8:50:27
 * @since 1.0
 */
public class SimpleEventBus implements EventBus {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEventBus.class);
    private final Set<EventListener> listeners = new CopyOnWriteArraySet<EventListener>();
    private final SimpleEventBusStatistics statistics = new SimpleEventBusStatistics();

    /**
     * 初始化eventbus 并  注册 监视器
     */
    public SimpleEventBus() {
        MonitorRegistry.registerMonitoringBean(statistics, SimpleEventBus.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(EventListener eventListener) {
        String listenerType = classNameOf(eventListener);
        if (listeners.remove(eventListener)) {
            statistics.recordUnregisteredListener(listenerType);
            logger.debug("EventListener {} unsubscribed successfully", listenerType);
        } else {
            logger.info("EventListener {} not removed. It was already unsubscribed", listenerType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(EventListener eventListener) {
        String listenerType = classNameOf(eventListener);
        if (listeners.add(eventListener)) {
            statistics.listenerRegistered(listenerType);
            logger.debug("EventListener [{}] subscribed successfully", listenerType);
        } else {
            logger.info("EventListener [{}] not added. It was already subscribed", listenerType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
	@Override
    public void publish(EventProxy... events) {
        statistics.recordPublishedEvent();
        if (!listeners.isEmpty()) {
            for (EventProxy event : events) {
                for (EventListener listener : listeners) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Dispatching Event [{}] to EventListener [{}]",
                                     event.getPayloadType().getSimpleName(), classNameOf(listener));
                    }
                    listener.handle(event);
                }
            }
        }
    }

    private String classNameOf(EventListener eventListener) {
        Class<?> listenerType;
        if (eventListener instanceof EventListenerProxy) {
            listenerType = ((EventListenerProxy) eventListener).getTargetType();
        } else {
            listenerType = eventListener.getClass();
        }
        return listenerType.getSimpleName();
    }

}
