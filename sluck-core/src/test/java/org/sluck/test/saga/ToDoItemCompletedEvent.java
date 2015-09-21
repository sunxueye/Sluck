
package org.sluck.test.saga;

import org.sluckframework.domain.identifier.DefaultIdentifier;

public class ToDoItemCompletedEvent {

    private final DefaultIdentifier todoId;

    public ToDoItemCompletedEvent(DefaultIdentifier todoId) {
        this.todoId = todoId;
    }

    public DefaultIdentifier getTodoId() {
        return todoId;
    }

    @Override
    public String toString() {
        return "ToDoItemCompletedEvent(" + todoId + ")";
    }
}
