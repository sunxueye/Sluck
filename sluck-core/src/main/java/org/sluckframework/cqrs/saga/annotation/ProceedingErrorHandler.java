package org.sluckframework.cqrs.saga.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.cqrs.eventhanding.async.RetryPolicy;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.domain.event.EventProxy;

/**
 * errorhandler的 prceed 实现,纪录日志,并继续执行
 * 
 * Author: sunxy
 * Created: 2015-09-15 22:54
 * Since: 1.0
 */
public class ProceedingErrorHandler implements ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProceedingErrorHandler.class);

    @Override
    public RetryPolicy onErrorPreparing(Class<? extends Saga> sagaType, EventProxy<?> publishedEvent,
                                        int invocationCount, Exception e) {
        logger.error("An error occurred while trying to prepare sagas of type [{}] for invocation of event [{}]. "
                        + "Proceeding with the next event.",
                sagaType.getName(), publishedEvent.getPayloadType().getName(), e);
        return RetryPolicy.proceed();
    }

    @Override
    public RetryPolicy onErrorInvoking(Saga saga, EventProxy<?> publishedEvent, int invocationCount, Exception e) {
        logger.error("Saga threw an exception while handling an Event. Ignoring and moving on...", e);
        return RetryPolicy.proceed();
    }
}