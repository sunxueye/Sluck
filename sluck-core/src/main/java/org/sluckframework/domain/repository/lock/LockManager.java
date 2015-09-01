package org.sluckframework.domain.repository.lock;

import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 管理锁的接口，
 * 
 * @author sunxy
 * @time 2015年9月1日 下午11:10:02
 * @since 1.0
 */
public interface LockManager {

    /**
     * 保证当前的线程 持有 指定聚合的 锁
     * @param aggregate the aggregate to validate the lock for
     * @return true if a valid lock is held, false otherwise
     */
    @SuppressWarnings("rawtypes")
	boolean validateLock(AggregateRoot aggregate);

    /**
     * 获取指定聚合标示符 代表的聚合的锁
     * 根据锁策略，可能是立即返回也可能一直阻塞等待
     *
     * @param aggregateIdentifier the identifier of the aggregate to obtains a lock for.
     */
    void obtainLock(Identifier<?> aggregateIdentifier);

    /**
     * 释放指定聚合标示符的锁
     * 在调用这个方法之前，必须保证已经获取到了聚合的锁
     *
     * @param aggregateIdentifier the identifier of the aggregate to release the lock for.
     */
    void releaseLock(Identifier<?> aggregateIdentifier);

}
