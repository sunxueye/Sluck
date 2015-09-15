package org.sluckframework.cqrs.saga.annotation;

import org.sluckframework.cqrs.eventhanding.async.RetryPolicy;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.domain.event.EventProxy;

/**
 * 错误处理,重试机制
 * 
 * Author: sunxy
 * Created: 2015-09-15 22:09
 * Since: 1.0
 */
public interface ErrorHandler {

    /**
     * 当执行 saga 出错时的重试机制
     *
     * @param sagaType        The type of Saga to prepare
     * @param publishedEvent  The event being published
     * @param invocationCount The number of attempts to prepare (is always at least 1)
     * @param exception       The exception that occurred in this attempt
     * @return the expected behavior for the event handling component
     */
    RetryPolicy onErrorPreparing(Class<? extends Saga> sagaType, EventProxy<?> publishedEvent,
                                 int invocationCount, Exception exception);

    /**
     * 当执行 saga 出错时的重试机制
     *
     * @param saga            The Saga instance being invoked
     * @param publishedEvent  The event handled by the Saga
     * @param invocationCount The number of times this event has been offered to the Saga, including the last, failed,
     *                        attempt
     * @param exception       The exception that occurred in the last attempt to invoke the Saga
     * @return The Policy describing what the SagaManager should do with this exception (retry, skip, etc)
     */
    RetryPolicy onErrorInvoking(Saga saga, EventProxy<?> publishedEvent, int invocationCount, Exception exception);
}
