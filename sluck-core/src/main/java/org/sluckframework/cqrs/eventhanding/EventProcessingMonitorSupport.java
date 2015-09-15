package org.sluckframework.cqrs.eventhanding;

/**
 * 提供监控的支持
 *
 * Author: sunxy
 * Created: 2015-09-15 22:47
 * Since: 1.0
 */
public interface EventProcessingMonitorSupport {

    void subscribeEventProcessingMonitor(EventProcessingMonitor monitor);

    void unsubscribeEventProcessingMonitor(EventProcessingMonitor monitor);
}
