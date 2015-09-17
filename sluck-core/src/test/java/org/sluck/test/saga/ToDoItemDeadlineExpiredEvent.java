
package org.sluck.test.saga;


public class ToDoItemDeadlineExpiredEvent {

    private final String todoId;

    public ToDoItemDeadlineExpiredEvent(String todoId) {
        this.todoId = todoId;
    }

    public String getTodoId() {
        return todoId;
    }

    @Override
    public String toString() {
        return "ToDoItemDeadlineExpiredEvent(" + todoId + ")";
    }
}
