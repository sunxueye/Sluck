package org.sluckframework.cqrs.eventhanding;

import org.sluckframework.domain.event.EventProxy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 事件处理监控的实现
 * 
 * Author: sunxy
 * Created: 2015-09-15 22:52
 * Since: 1.0
 */
public class EventProcessingMonitorCollection implements EventProcessingMonitor, EventProcessingMonitorSupport {

    private final Set<EventProcessingMonitor> delegates = new CopyOnWriteArraySet<EventProcessingMonitor>();

    @Override
    public void onEventProcessingCompleted(List<? extends EventProxy<?>> eventMessages) {
        for (EventProcessingMonitor delegate : delegates) {
            delegate.onEventProcessingCompleted(eventMessages);
        }
    }

    @Override
    public void onEventProcessingFailed(List<? extends EventProxy<?>> eventMessages, Throwable cause) {
        for (EventProcessingMonitor delegate : delegates) {
            delegate.onEventProcessingFailed(eventMessages, cause);
        }
    }

    @Override
    public void subscribeEventProcessingMonitor(EventProcessingMonitor monitor) {
        delegates.add(monitor);
    }

    @Override
    public void unsubscribeEventProcessingMonitor(EventProcessingMonitor monitor) {
        delegates.remove(monitor);
    }
}