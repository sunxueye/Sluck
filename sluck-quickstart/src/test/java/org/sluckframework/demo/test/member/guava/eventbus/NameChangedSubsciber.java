package org.sluckframework.demo.test.member.guava.eventbus;

import com.google.common.eventbus.Subscribe;

/**
 * Author: sunxy
 * Created: 2016-01-04 23:49
 * Since: 1.0
 */
public class NameChangedSubsciber {
    @Subscribe
    public void apply(NameChangedEvent event) {
        System.out.println("name changed, old name:" + event.getOldName() + ", new name:" + event.getNewName());
    }
}
