
package org.sluck.test.saga;

public class ToDoItemCompletedEvent {

    private final String todoId;

    public ToDoItemCompletedEvent(String todoId) {
        this.todoId = todoId;
    }

    public String getTodoId() {
        return todoId;
    }

    @Override
    public String toString() {
        return "ToDoItemCompletedEvent(" + todoId + ")";
    }
}
