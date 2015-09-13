package org.sluckframework.cqrs.saga;

import org.sluckframework.cqrs.eventhanding.EventListenerProxy;
import org.sluckframework.domain.event.EventProxy;

/**
 * saga管理器,可以重定向事件到指定的saga实例,负责管理saga的生命周期,此实例需要线程安全
 *
 * Author: sunxy
 * Created: 2015-09-13 14:49
 * Since: 1.0
 */
public interface SagaManager extends EventListenerProxy {

    /**
     * 将事件交与 与指定关联值相关的saga
     *
     * @param event the event to handle
     */
    @Override
    void handle(EventProxy<?> event);
}
