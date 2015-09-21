
package org.sluck.test.saga;


import org.sluckframework.cqrs.commandhandling.annotation.TargetAggregateIdentifier;
import org.sluckframework.domain.identifier.DefaultIdentifier;

public class CreateToDoItemCommand {

    @TargetAggregateIdentifier
    private final DefaultIdentifier todoId;
    private final String description;

    public CreateToDoItemCommand(DefaultIdentifier todoId, String description) {
        this.todoId = todoId;
        this.description = description;
    }

    public DefaultIdentifier getTodoId() {
        return todoId;
    }

    public String getDescription() {
        return description;
    }
}
