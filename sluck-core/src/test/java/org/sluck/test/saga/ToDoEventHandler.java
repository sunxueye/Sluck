package org.sluck.test.saga;

import org.joda.time.DateTime;
import org.sluckframework.cqrs.eventhanding.annotation.EventHandler;
import org.sluckframework.cqrs.eventhanding.annotation.Timestamp;

/**
 * Author: sunxy
 * Created: 2015-09-21 22:41
 * Since: 1.0
 */
public class ToDoEventHandler {

    @EventHandler
    public void handle(ToDoItemCreatedEvent event, @Timestamp DateTime time) {
        System.out.println(String.format("We've got something to do: %s (%s, created at %s)",
                event.getDescription(),
                event.getTodoId(),
                time.toString("d-M-y H:m")));
    }

    @EventHandler
    public void handle(ToDoItemCompletedEvent event) {
        System.out.println(String.format("We've completed the task with id %s", event.getTodoId()));
    }
}
