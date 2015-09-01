package org.sluckframework.common.lock;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 标示符锁机制，每个标示符都对应一个锁，一个线程可以获取锁多次，但是也必须释放相应的次数别的线程才能获取这个聚合的锁
 * 
 * @author sunxy
 * @time 2015年9月1日 下午11:17:05
 * @since 1.0
 */
public class IdentifierBasedLock {

    private static final Set<IdentifierBasedLock> INSTANCES =
            newSetFromMap(synchronizedMap(new WeakHashMap<IdentifierBasedLock, Boolean>()));

    private final ConcurrentHashMap<String, DisposableLock> locks = new ConcurrentHashMap<String, DisposableLock>();

    /**
     * 创建一个新的标示符锁实例
     */
    public IdentifierBasedLock() {
        INSTANCES.add(this);
    }

    private static Set<Thread> threadsWaitingForMyLocks(Thread owner) {
        return threadsWaitingForMyLocks(owner, INSTANCES);
    }

    private static Set<Thread> threadsWaitingForMyLocks(Thread owner, Set<IdentifierBasedLock> locksInUse) {
        Set<Thread> waitingThreads = new HashSet<Thread>();
        for (IdentifierBasedLock lock : locksInUse) {
            for (DisposableLock disposableLock : lock.locks.values()) {
                if (disposableLock.isHeldBy(owner)) {
                    final Collection<Thread> c = disposableLock.queuedThreads();
                    for (Thread thread : c) {
                        if (waitingThreads.add(thread)) {
                            waitingThreads.addAll(threadsWaitingForMyLocks(thread, locksInUse));
                        }
                    }
                }
            }
        }
        return waitingThreads;
    }

    /**
     * 判断当前的 标示符 是否有对应的锁
     * 
     * @param identifier The identifier of the lock to verify
     * @return <code>true</code> if the current thread holds a lock, otherwise <code>false</code>
     */
    public boolean hasLock(String identifier) {
        return isLockAvailableFor(identifier)
                && lockFor(identifier).isHeldByCurrentThread();
    }

    /**
     * 尝试获取标示符对应的锁，会一直阻塞直到获取
     *
     * @param identifier the identifier of the lock to obtain.
     */
    public void obtainLock(String identifier) {
        boolean lockObtained = false;
        while (!lockObtained) {
            DisposableLock lock = lockFor(identifier);
            lockObtained = lock.lock();
            if (!lockObtained) {
                locks.remove(identifier, lock);
            }
        }
    }

    /**
     * 释放锁，如果当前线程没有持有对应的锁，则抛出异常
     *
     * @param identifier the identifier to release the lock for.
     * @throws IllegalStateException        if no lock was ever obtained for this aggregate
     */
    public void releaseLock(String identifier) {
        if (!locks.containsKey(identifier)) {
            throw new IllegalLockUsageException("No lock for this identifier was ever obtained");
        }
        DisposableLock lock = lockFor(identifier);
        lock.unlock(identifier);
    }

    private boolean isLockAvailableFor(String identifier) {
        return locks.containsKey(identifier);
    }

    private DisposableLock lockFor(String identifier) {
        DisposableLock lock = locks.get(identifier);
        while (lock == null) {
            locks.putIfAbsent(identifier, new DisposableLock());
            lock = locks.get(identifier);
        }
        return lock;
    }

    private final class DisposableLock {

        private final PubliclyOwnedReentrantLock lock;
        // guarded by "lock"
        private boolean isClosed = false;

        private DisposableLock() {
            this.lock = new PubliclyOwnedReentrantLock();
        }

        private boolean isHeldByCurrentThread() {
            return lock.isHeldByCurrentThread();
        }

        private void unlock(String identifier) {
            try {
                lock.unlock();
            } finally {
                disposeIfUnused(identifier);
            }
        }

        private boolean lock() {
            try {
                if (!lock.tryLock(0, TimeUnit.NANOSECONDS)) {
                    do {
                        checkForDeadlock();
                    } while (!lock.tryLock(100, TimeUnit.MILLISECONDS));
                }
            } catch (InterruptedException e) {
                throw new LockAcquisitionFailedException("Thread was interrupted", e);
            }
            if (isClosed) {
                lock.unlock();
                return false;
            }
            return true;
        }

        private void checkForDeadlock() {
            if (!lock.isHeldByCurrentThread() && lock.isLocked()) {
                for (Thread thread : threadsWaitingForMyLocks(Thread.currentThread())) {
                    if (lock.isHeldBy(thread)) {
                        throw new DeadlockException(
                                "An imminent deadlock was detected while attempting to acquire a lock");
                    }
                }
            }
        }

        private void disposeIfUnused(String identifier) {
            if (lock.tryLock()) {
                try {
                    if (lock.getHoldCount() == 1) {
                        // we now have a lock. We can shut it down.
                        isClosed = true;
                        locks.remove(identifier, this);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        public Collection<Thread> queuedThreads() {
            return lock.getQueuedThreads();
        }

        public boolean isHeldBy(Thread owner) {
            return lock.isHeldBy(owner);
        }
    }

    private static final class PubliclyOwnedReentrantLock extends ReentrantLock {

        private static final long serialVersionUID = -2259228494514612163L;

        @Override
        public Collection<Thread> getQueuedThreads() { // NOSONAR
            return super.getQueuedThreads();
        }

        public boolean isHeldBy(Thread thread) {
            return thread.equals(getOwner());
        }
    }

}
