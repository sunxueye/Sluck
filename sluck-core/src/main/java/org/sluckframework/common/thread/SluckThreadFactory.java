package org.sluckframework.common.thread;

import java.util.concurrent.ThreadFactory;

import org.sluckframework.common.exception.Assert;

/**
 * sluck的线程工厂
 * 
 * @author sunxy
 * @time 2015年9月7日 下午11:00:19
 * @since 1.0
 */
public class SluckThreadFactory implements ThreadFactory {

    private final int priority;
    private final ThreadGroup groupName;

    /**
     * 使用指定的 name 初始化，并使用默认的优先度 5
     *
     * @param groupName The name of the group to create each thread in
     */
    public SluckThreadFactory(String groupName) {
        this(new ThreadGroup(groupName));
    }

    /**
     * 使用指定的 ThreadGroup 初始化
     *
     * @param group The ThreadGroup to create each thread in
     */
    public SluckThreadFactory(ThreadGroup group) {
        this(Thread.NORM_PRIORITY, group);
    }

    /**
     * 使用指定的 优先度 和 ThreadGroup 初始化
     *
     * @param priority The priority of the threads to create
     * @param group    The ThreadGroup to create each thread in
     */
    public SluckThreadFactory(int priority, ThreadGroup group) {
        Assert.isTrue(priority <= Thread.MAX_PRIORITY && priority >= Thread.MIN_PRIORITY, "Given priority is invalid");
        this.priority = priority;
        this.groupName = group;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(groupName, r);
        thread.setPriority(priority);
        return thread;
    }
}
