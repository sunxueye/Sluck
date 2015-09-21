
package org.sluck.test.saga;


import org.sluckframework.domain.identifier.DefaultIdentifier;

public class ToDoItemDeadlineExpiredEvent {

    private final DefaultIdentifier todoId;

    public ToDoItemDeadlineExpiredEvent(DefaultIdentifier todoId) {
        this.todoId = todoId;
    }

    public DefaultIdentifier getTodoId() {
        return todoId;
    }

    @Override
    public String toString() {
        return "ToDoItemDeadlineExpiredEvent(" + todoId + ")";
    }
}
