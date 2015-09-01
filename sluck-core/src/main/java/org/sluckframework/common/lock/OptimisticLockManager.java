package org.sluckframework.common.lock;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.lock.LockManager;

/**
 * 乐观所的实现。使用最后提交的事件的 sequence nubmer来 判断 并发操作
 * 
 * @author sunxy
 * @time 2015年9月2日 上午12:19:06
 * @since 1.0
 */
public class OptimisticLockManager implements LockManager {

    private final ConcurrentHashMap<Object, OptimisticLock> locks = new ConcurrentHashMap<Object, OptimisticLock>();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
	@Override
    public boolean validateLock(AggregateRoot aggregate) {
        OptimisticLock lock = locks.get(aggregate.getIdentifier());
        return lock != null && lock.validate(aggregate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void obtainLock(Identifier<?> aggregateIdentifier) {
        boolean obtained = false;
        while (!obtained) {
            locks.putIfAbsent(aggregateIdentifier, new OptimisticLock());
            OptimisticLock lock = locks.get(aggregateIdentifier);
            obtained = lock != null && lock.lock();
            if (!obtained) {
                locks.remove(aggregateIdentifier, lock);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseLock(Identifier<?> aggregateIdentifier) {
        OptimisticLock lock = locks.get(aggregateIdentifier);
        if (lock != null) {
            lock.unlock(aggregateIdentifier);
        }
    }

    private final class OptimisticLock {

        private Long versionNumber;
        private final Map<Thread, Integer> threadsHoldingLock = new WeakHashMap<Thread, Integer>();
        private boolean closed = false;

        private OptimisticLock() {
        }

        @SuppressWarnings("rawtypes")
		private synchronized boolean validate(AggregateRoot aggregate) {
            Long lastCommittedEventSequenceNumber = aggregate.getVersion();
            if (versionNumber == null || versionNumber.equals(lastCommittedEventSequenceNumber)) {
                long last = lastCommittedEventSequenceNumber == null ? 0 : lastCommittedEventSequenceNumber;
                versionNumber = last + aggregate.getUncommittedEventCount();
                return true;
            }
            return false;
        }

        private synchronized boolean lock() {
            if (closed) {
                return false;
            }
            Integer lockCount = threadsHoldingLock.get(Thread.currentThread());
            if (lockCount == null) {
                lockCount = 0;
            }
            threadsHoldingLock.put(Thread.currentThread(), lockCount + 1);
            return true;
        }

        private synchronized void unlock(Object aggregateIdentifier) {
            Integer lockCount = threadsHoldingLock.get(Thread.currentThread());
            if (lockCount == null || lockCount == 1) {
                threadsHoldingLock.remove(Thread.currentThread());
            } else {
                threadsHoldingLock.put(Thread.currentThread(), lockCount - 1);
            }
            if (threadsHoldingLock.isEmpty()) {
                closed = true;
                locks.remove(aggregateIdentifier, this);
            }
        }
    }

}
