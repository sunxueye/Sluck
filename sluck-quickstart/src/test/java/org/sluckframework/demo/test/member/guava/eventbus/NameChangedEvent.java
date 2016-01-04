package org.sluckframework.demo.test.member.guava.eventbus;

/**
 * Author: sunxy
 * Created: 2016-01-04 23:47
 * Since: 1.0
 */
public class NameChangedEvent {

    private String newName;
    private String oldName;

    public NameChangedEvent(String newName, String oldName) {
        this.newName = newName;
        this.oldName = oldName;
    }

    public String getNewName() {
        return newName;
    }

    public String getOldName() {
        return oldName;
    }
}
