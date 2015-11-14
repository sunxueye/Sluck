package org.sluck.test.saga;

import org.joda.time.Duration;
import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.cqrs.eventhanding.scheduling.EventScheduler;
import org.sluckframework.cqrs.eventhanding.scheduling.ScheduleToken;
import org.sluckframework.cqrs.saga.annotation.AbstractAnnotatedSaga;
import org.sluckframework.cqrs.saga.annotation.EndSaga;
import org.sluckframework.cqrs.saga.annotation.SagaEventHandler;
import org.sluckframework.cqrs.saga.annotation.StartSaga;

import javax.annotation.Resource;

/**
 * @author Allard Buijze
 */
public class ToDoSaga extends AbstractAnnotatedSaga {

    private static final long serialVersionUID = 1798051388403504162L;

    private transient CommandGateway commandGateway;
    private transient EventScheduler eventScheduler;

    private ScheduleToken deadline;

    public ToDoSaga() {
        super(null);
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "todoId")
    public void onToDoItemCreated(ToDoItemCreatedEvent event) {
        deadline = eventScheduler.schedule(Duration.standardSeconds(2),
                                           new ToDoItemDeadlineExpiredEvent(event.getTodoId()));
    }

    @SagaEventHandler(associationProperty = "todoId")
    public void onDeadlineExpired(ToDoItemDeadlineExpiredEvent event) {
        commandGateway.send(new MarkToDoItemOverdueCommand(event.getTodoId()));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "todoId")
    public void onToDoItemCompleted(ToDoItemCompletedEvent event) {
        if (deadline != null) {
            eventScheduler.cancelSchedule(deadline);
        }
    }

    @Resource
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Resource
    public void setEventScheduler(EventScheduler eventScheduler) {
        this.eventScheduler = eventScheduler;
    }
}
