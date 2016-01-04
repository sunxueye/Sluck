package org.sluckframework.demo.test.member.guava.eventbus;

import com.google.common.eventbus.EventBus;

/**
 * Author: sunxy
 * Created: 2016-01-04 23:53
 * Since: 1.0
 */
public class EventBusMainTest {
    public static void main(String[] args) {
        EventBus bus = new EventBus();
        bus.register(new NameChangedSubsciber());

        bus.post(new NameChangedEvent("cx", "cxy"));
    }
}
