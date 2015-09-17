package org.sluckframework.cqrs.eventhanding.scheduling.java;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventhanding.scheduling.EventScheduler;
import org.sluckframework.cqrs.eventhanding.scheduling.ScheduleToken;
import org.sluckframework.cqrs.unitofwork.DefaultUnitOfWorkFactory;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkFactory;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.aggregate.GenericEvent;
import org.sluckframework.domain.identifier.IdentifierFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 基于java的 publish event 简单实现
 * 
 * Author: sunxy
 * Created: 2015-09-17 22:42
 * Since: 1.0
 */
public class SimpleEventScheduler implements EventScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEventScheduler.class);

    private final ScheduledExecutorService executorService;
    private final EventBus eventBus;
    private final UnitOfWorkFactory unitOfWorkFactory;
    private final Map<String, Future<?>> tokens = new ConcurrentHashMap<String, Future<?>>();

    /**
     * Initialize the SimpleEventScheduler using the given <code>executorService</code> as trigger and execution
     * mechanism, and publishes events to the given <code>eventBus</code>.
     *
     * @param executorService The backing ScheduledExecutorService
     * @param eventBus        The Event Bus on which Events are to be published
     */
    public SimpleEventScheduler(ScheduledExecutorService executorService, EventBus eventBus) {
        this(executorService, eventBus, new DefaultUnitOfWorkFactory());
    }

    /**
     * Initialize the SimpleEventScheduler using the given <code>executorService</code> as trigger and execution
     * mechanism, and publishes events to the given <code>eventBus</code>. The <code>eventTriggerCallback</code> is
     * invoked just before and after publication of a scheduled event.
     *
     * @param executorService   The backing ScheduledExecutorService
     * @param eventBus          The Event Bus on which Events are to be published
     * @param unitOfWorkFactory The factory that creates the Unit of Work to manage transactions
     */
    public SimpleEventScheduler(ScheduledExecutorService executorService, EventBus eventBus,
                                UnitOfWorkFactory unitOfWorkFactory) {
        Assert.notNull(executorService, "executorService may not be null");
        Assert.notNull(eventBus, "eventBus may not be null");
        Assert.notNull(unitOfWorkFactory, "unitOfWorkFactory may not be null");

        this.executorService = executorService;
        this.eventBus = eventBus;
        this.unitOfWorkFactory = unitOfWorkFactory;
    }

    @Override
    public ScheduleToken schedule(DateTime triggerDateTime, Object event) {
        return schedule(new Duration(null, triggerDateTime), event);
    }

    @Override
    public ScheduleToken schedule(Duration triggerDuration, Object event) {
        String tokenId = IdentifierFactory.getInstance().generateIdentifier();
        ScheduledFuture<?> future = executorService.schedule(new PublishEventTask(event, tokenId),
                triggerDuration.getMillis(),
                TimeUnit.MILLISECONDS);
        tokens.put(tokenId, future);
        return new SimpleScheduleToken(tokenId);
    }

    @Override
    public void cancelSchedule(ScheduleToken scheduleToken) {
        if (!SimpleScheduleToken.class.isInstance(scheduleToken)) {
            throw new IllegalArgumentException("The given ScheduleToken was not provided by this scheduler.");
        }
        Future<?> future = tokens.remove(((SimpleScheduleToken) scheduleToken).getTokenId());
        if (future != null) {
            future.cancel(false);
        }
    }

    private class PublishEventTask implements Runnable {

        private final Object event;
        private final String tokenId;

        public PublishEventTask(Object event, String tokenId) {
            this.event = event;
            this.tokenId = tokenId;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            EventProxy<?> eventMessage = createMessage();
            if (logger.isInfoEnabled()) {
                logger.info("Triggered the publication of event [{}]", eventMessage.getPayloadType().getSimpleName());
            }
            UnitOfWork unitOfWork = unitOfWorkFactory.createUnitOfWork();
            try {
                unitOfWork.publishEvent(eventMessage, eventBus);
                unitOfWork.commit();
            } finally {
                tokens.remove(tokenId);
            }
        }

        /**
         * Creates a new message for the scheduled event. This ensures that a new identifier and timestamp will always
         * be generated, so that the timestamp will reflect the actual moment the trigger occurred.
         *
         * @return the message to publish
         */
        private EventProxy<?> createMessage() {
            EventProxy<?> eventMessage;
            if (event instanceof EventProxy) {
                eventMessage = new GenericEvent<>(((EventProxy) event).getPayload());
            } else {
                eventMessage = new GenericEvent<>(event);
            }
            return eventMessage;
        }
    }
}
