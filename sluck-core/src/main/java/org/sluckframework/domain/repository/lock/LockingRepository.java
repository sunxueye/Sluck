package org.sluckframework.domain.repository.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListenerAdapter;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.AbstractRepository;
import org.sluckframework.domain.repository.ConcurrencyException;


/**
 * 为了阻止聚合根的并发操作，实现锁机制的仓储。除非真正的持久化机制能够实现锁机制组织聚合并发的产生，否则推荐使用锁仓储
 * 支持两种机制，悲观和乐观，默认是悲观锁机制
 * 
 * @author sunxy
 * @time 2015年9月1日 下午11:02:59
 * @since 1.0
 */
public abstract class LockingRepository<T extends AggregateRoot<ID>, ID extends Identifier<?>> extends AbstractRepository<T,ID> {

    private static final Logger logger = LoggerFactory.getLogger(LockingRepository.class);

    private final LockManager lockManager;

    /**
     * 使用默认的悲观锁机制 和指定的聚合类型 初始化
     * @param aggregateType The type of aggregate stored in this repository
     */
    protected LockingRepository(Class<T> aggregateType) {
        this(aggregateType, new PessimisticLockManager());
    }

    /**
     * 使用 指定的聚合类型 和 锁机制 初始化
     *
     * @param aggregateType The type of aggregate stored in this repository
     * @param lockManager the lock manager to use
     */
    protected LockingRepository(Class<T> aggregateType, LockManager lockManager) {
        super(aggregateType);
        Assert.notNull(lockManager, "lockManager may not be null");
        this.lockManager = lockManager;
    }

    @Override
    public void add(T aggregate) {
        final Identifier<?> aggregateIdentifier = aggregate.getIdentifier();
        lockManager.obtainLock(aggregateIdentifier);
        try {
            super.add(aggregate);
            CurrentUnitOfWork.get().registerListener(new LockCleaningListener(aggregateIdentifier));
        } catch (RuntimeException ex) {
            logger.debug("Exception occurred while trying to add an aggregate. Releasing lock.", ex);
            lockManager.releaseLock(aggregateIdentifier);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws AggregateNotFoundException if aggregate with given id cannot be found
     * @throws RuntimeException           any exception thrown by implementing classes
     */
    @Override
    public T load(ID aggregateIdentifier, Long expectedVersion) {
        lockManager.obtainLock(aggregateIdentifier);
        try {
            final T aggregate = super.load(aggregateIdentifier, expectedVersion);
            CurrentUnitOfWork.get().registerListener(new LockCleaningListener(aggregateIdentifier));
            return aggregate;
        } catch (RuntimeException ex) {
            logger.debug("Exception occurred while trying to load an aggregate. Releasing lock.", ex);
            lockManager.releaseLock(aggregateIdentifier);
            throw ex;
        }
    }

    /**
     * 验证 是否 获取聚合的 锁后  执行 doSaveWithLock
     *
     * @param aggregate the aggregate to store
     */
    @Override
    protected final void doSave(T aggregate) {
        if (aggregate.getVersion() != null && !lockManager.validateLock(aggregate)) {
            throw new ConcurrencyException(String.format(
                    "The aggregate of type [%s] with identifier [%s] could not be "
                            + "saved, as a valid lock is not held. Either another thread has saved an aggregate, or "
                            + "the current thread had released its lock earlier on.",
                    aggregate.getClass().getSimpleName(),
                    aggregate.getIdentifier()));
        }
        doSaveWithLock(aggregate);
    }

    /**
     * 验证 是否 获取聚合的 锁后  执行 doDeleteWithLock
     *
     * @param aggregate the aggregate to delete
     */
    @Override
    protected final void doDelete(T aggregate) {
        if (aggregate.getVersion() != null && !lockManager.validateLock(aggregate)) {
            throw new ConcurrencyException(String.format(
                    "The aggregate of type [%s] with identifier [%s] could not be "
                            + "saved, as a valid lock is not held. Either another thread has saved an aggregate, or "
                            + "the current thread had released its lock earlier on.",
                    aggregate.getClass().getSimpleName(),
                    aggregate.getIdentifier()));
        }
        doDeleteWithLock(aggregate);
    }

    /**
     * 所有的锁已经获取，执行保存聚合的操作
     *
     * @param aggregate the aggregate to store
     */
    protected abstract void doSaveWithLock(T aggregate);

    /**
     * 所有的锁已获取，指定 delete 聚合的操作
     *
     * @param aggregate the aggregate to delete
     */
    protected abstract void doDeleteWithLock(T aggregate);

    /**
     * 已获取所有的锁，执行真正的聚合加载的动作
     *
     * @param aggregateIdentifier the identifier of the aggregate to load
     * @param expectedVersion     The expected version of the aggregate
     * @return the fully initialized aggregate
     */
    @Override
    protected abstract T doLoad(ID aggregateIdentifier, Long expectedVersion);

    /**
     * 释放锁的 uow 适配器， 在onCleanUp阶段 清除  
     * after committed or rolled back
     */
    private class LockCleaningListener extends UnitOfWorkListenerAdapter {

        private final Identifier<?> aggregateIdentifier;

        public LockCleaningListener(Identifier<?> aggregateIdentifier) {
            this.aggregateIdentifier = aggregateIdentifier;
        }

        @Override
        public void onCleanup(UnitOfWork unitOfWork) {
            lockManager.releaseLock(aggregateIdentifier);
        }
    }

}
