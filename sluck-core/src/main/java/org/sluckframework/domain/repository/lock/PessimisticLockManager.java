package org.sluckframework.domain.repository.lock;

import org.sluckframework.common.lock.IdentifierBasedLock;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.identifier.Identifier;


/**
 * 悲观锁机制的实现，保证线程的独占操作
 * 
 * @author sunxy
 * @time 2015年9月1日 下午11:14:54
 * @since 1.0
 */
public class PessimisticLockManager implements LockManager{

    private final IdentifierBasedLock lock = new IdentifierBasedLock();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
	@Override
    public boolean validateLock(AggregateRoot aggregate) {
        Object aggregateIdentifier = aggregate.getIdentifier();
        return lock.hasLock(aggregateIdentifier.toString());
    }

    /**
     * 获取锁，如果获取不到，则一直堵塞
     *
     * @param aggregateIdentifier the identifier of the aggregate to obtains a lock for.
     */
    @Override
    public void obtainLock(Identifier<?> aggregateIdentifier) {
        lock.obtainLock(aggregateIdentifier.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseLock(Identifier<?> aggregateIdentifier) {
        lock.releaseLock(aggregateIdentifier.toString());
    }

}
