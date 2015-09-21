package org.sluck.test.saga;


import org.sluckframework.cqrs.commandhandling.annotation.TargetAggregateIdentifier;
import org.sluckframework.domain.identifier.DefaultIdentifier;

public class MarkCompletedCommand {

    @TargetAggregateIdentifier
    private final DefaultIdentifier todoId;

    public MarkCompletedCommand(DefaultIdentifier todoId) {
        this.todoId = todoId;
    }

    public DefaultIdentifier getTodoId() {
        return todoId;
    }
}
