package org.sluckframework.demo.test.member.guava;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author: sunxy
 * Created: 2015-12-22 17:48
 * Since: 1.0
 */
public class SerivceDemo {

    public static void abstractService() {
        AbstractService service = new AbstractService() {
            @Override
            protected void doStart() {
                System.out.println("start");
            }

            @Override
            protected void doStop() {
                System.out.println("stop");
            }
        };

        service.startAsync();
    }

    public static void idleService() {
        AbstractIdleService service = new AbstractIdleService() {
            @Override
            protected void startUp() throws Exception {
                System.out.println("start");
            }

            @Override
            protected void shutDown() throws Exception {
                System.out.println("shutdown");
            }

            @Override
            protected Executor executor() {
                return Executors.newCachedThreadPool();
            }
        };
        service.startAsync();
    }

    public static void scheduledService() {
        AbstractScheduledService scheduledService = new AbstractScheduledService() {
            @Override
            protected void runOneIteration() throws Exception {
                System.out.println("one task");
            }

            @Override
            protected Scheduler scheduler() {
                return Scheduler.newFixedDelaySchedule(100, 1000, TimeUnit.MILLISECONDS);
            }
        };

        scheduledService.startAsync();

        //使用ServiceManager可以管理多个service
        Iterable<Service> iterable = Sets.newHashSet(scheduledService);
        ServiceManager manager = new ServiceManager(iterable);
        manager.startAsync();
    }



    public static void main(String[] args) {
//        abstractService();
//        idleService();
//        scheduledService();
    }
}
