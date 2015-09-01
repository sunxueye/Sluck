package org.sluckframework.domain.repository.lock;

import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 无锁的实现
 * 
 * @author sunxy
 * @time 2015年9月2日 上午12:18:07
 * @since 1.0
 */
public class NullLockManager implements LockManager {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean validateLock(AggregateRoot aggregate) {
		return true;
	}

	@Override
	public void obtainLock(Identifier<?> aggregateIdentifier) {

	}

	@Override
	public void releaseLock(Identifier<?> aggregateIdentifier) {
	}

}
