package org.sluckframework.cqrs.unitofwork;

/**
 * uow factory 的默认实现
 * 
 * @author sunxy
 * @time 2015年9月7日 上午9:49:10	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class DefaultUnitOfWorkFactory implements UnitOfWorkFactory {
    
	private final TransactionManager transactionManager;

    public DefaultUnitOfWorkFactory() {
        this(null);
    }

    /**
     * 使用 事务管理器初始化
     *
     * @param transactionManager The transaction manager to manage the transactions for Unit Of Work created by this
     *                           factory
     */
    public DefaultUnitOfWorkFactory(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public UnitOfWork createUnitOfWork() {
        return DefaultUnitOfWork.startAndGet(transactionManager);
    }
}
