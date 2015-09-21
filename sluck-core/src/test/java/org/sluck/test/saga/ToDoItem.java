package org.sluck.test.saga;

import org.sluckframework.cqrs.commandhandling.annotation.CommandHandler;
import org.sluckframework.cqrs.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.sluckframework.cqrs.eventsourcing.annotation.AggregateIdentifier;
import org.sluckframework.cqrs.eventsourcing.annotation.EventSourcingHandler;
import org.sluckframework.domain.identifier.DefaultIdentifier;


public class ToDoItem extends AbstractAnnotatedAggregateRoot {

    @AggregateIdentifier
    private DefaultIdentifier id;

    // No-arg constructor, required by Axon
    public ToDoItem() {
    }

    @CommandHandler
    public ToDoItem(CreateToDoItemCommand command) {
        apply(new ToDoItemCreatedEvent(command.getTodoId(), command.getDescription()));
    }

    @CommandHandler
    public void markCompleted(MarkCompletedCommand command) {
        apply(new ToDoItemCompletedEvent(command.getTodoId()));
    }

    @EventSourcingHandler
    public void on(ToDoItemCreatedEvent event) {
        this.id = event.getTodoId();
    }
}
