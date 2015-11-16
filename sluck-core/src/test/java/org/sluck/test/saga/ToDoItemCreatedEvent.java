package org.sluck.test.saga;

import org.sluckframework.domain.identifier.DefaultIdentifier;

public class ToDoItemCreatedEvent {

    private DefaultIdentifier todoId;
    private String description;

    private ToDoItemCreatedEvent(){}

    public ToDoItemCreatedEvent(DefaultIdentifier todoId, String description) {
        this.todoId = todoId;
        this.description = description;
    }

    public DefaultIdentifier getTodoId() {
        return todoId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ToDoItemCreatedEvent(" + todoId + ", '" + description + "')";
    }
}
