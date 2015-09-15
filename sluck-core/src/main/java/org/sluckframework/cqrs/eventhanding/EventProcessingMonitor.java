package org.sluckframework.cqrs.eventhanding;

import org.sluckframework.domain.event.EventProxy;

import java.util.List;

/**
 * 监控事件的处理
 * 
 * Author: sunxy
 * Created: 2015-09-15 22:48
 * Since: 1.0
 */
public interface EventProcessingMonitor {

    /**
     * 当订阅的事件被成功的处理的时候执行
     *
     * @param eventMessages The messages that have been successfully processed
     */
    void onEventProcessingCompleted(List<? extends EventProxy<?>> eventMessages);

    /**
     * 当订阅的事件被处理失败的时候执行
     *
     * @param eventMessages The message that failed
     * @param cause         The cause of the failure
     */
    void onEventProcessingFailed(List<? extends EventProxy<?>> eventMessages, Throwable cause);
}
