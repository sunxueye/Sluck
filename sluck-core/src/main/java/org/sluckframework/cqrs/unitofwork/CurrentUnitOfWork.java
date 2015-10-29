package org.sluckframework.cqrs.unitofwork;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 当前线程绑定的 工作单元， 可以管理事务 ，保证一次命令的 原子性
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:34:45
 * @since 1.0
 */
public abstract class CurrentUnitOfWork {

    private static final ThreadLocal<Deque<UnitOfWork>> CURRENT = new ThreadLocal<>();

    private CurrentUnitOfWork() {
    }

    /**
     * 判断 uow 是否开始
     *
     * @return whether a UnitOfWork has already been started.
     */
    public static boolean isStarted() {
        return CURRENT.get() != null && !CURRENT.get().isEmpty();
    }

    /**
     * 获取当前线程绑定 的 uow
     *
     * @return The UnitOfWork bound to the current thread.
     */
    public static UnitOfWork get() {
        if (isEmpty()) {
            throw new IllegalStateException("No UnitOfWork is currently started for this thread.");
        }
        return CURRENT.get().peek();
    }

    private static boolean isEmpty() {
        Deque<UnitOfWork> unitsOfWork = CURRENT.get();
        return unitsOfWork == null || unitsOfWork.isEmpty();
    }

    /**
     * 提交，如果还没开始 将抛异常
     *
     * @throws IllegalStateException if no UnitOfWork is currently started.
     */
    public static void commit() {
        get().commit();
    }

    /**
     * bing uow 到当前线程
     *
     * @param unitOfWork The UnitOfWork to bind to the current thread.
     */
    public static void set(UnitOfWork unitOfWork) {
        if (CURRENT.get() == null) {
            CURRENT.set(new LinkedList<>());
        }
        CURRENT.get().push(unitOfWork);
    }

    /**
     * 将指定 uow 与当前线程 解绑
     *
     * @param unitOfWork The UnitOfWork had bound to the current thread.
     */
    public static void clear(UnitOfWork unitOfWork) {
        if (isStarted() && CURRENT.get().peek() == unitOfWork) {
            CURRENT.get().pop();
            if (CURRENT.get().isEmpty()) {
                CURRENT.remove();
            }
        } else {
            throw new IllegalStateException("Could not clear this UnitOfWork. It is not the active one.");
        }
    }

}
