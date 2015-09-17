
package org.sluck.test.saga;

import org.sluckframework.cqrs.commandhandling.annotation.TargetAggregateIdentifier;


public class MarkToDoItemOverdueCommand  {

    @TargetAggregateIdentifier
    private final String todoId;

    public MarkToDoItemOverdueCommand(String todoId) {
        this.todoId = todoId;
    }

    public String getTodoId() {
        return todoId;
    }
}
