package org.sluckframework.cqrs.unitofwork;

import java.util.List;
import java.util.Set;

import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.event.EventProxy;

/**
 * 工作单元 监听器
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:39:39
 * @since 1.0
 */
public interface UnitOfWorkListener {
	
	/**
	 * 当 uow aftercommit 后执行
     *
     * @param unitOfWork The Unit of Work being committed
     */
    void afterCommit(UnitOfWork unitOfWork);

    /**
     * 当 uow 回滚时执行
     *
     * @param unitOfWork   The Unit of Work being rolled back
     * @param failureCause The exception (or error) causing the roll back
     */
    void onRollback(UnitOfWork unitOfWork, Throwable failureCause);

    /**
     * 当注册 需要发布的事件 执行
     *
     * @param unitOfWork The Unit of Work on which an event is registered
     * @param event      The event about to be registered for publication
     * @return the (modified) event to register for publication
     */
    <T> EventProxy<T> onEventRegistered(UnitOfWork unitOfWork, EventProxy<T> event);

    /**
     * 当 uow 预备提价 是执行
     *
     * @param unitOfWork     The Unit of Work being committed
     * @param aggregateRoots the aggregate roots being committed
     * @param events         Events that have been registered for dispatching
     */
    @SuppressWarnings("rawtypes")
	void onPrepareCommit(UnitOfWork unitOfWork, Set<AggregateRoot> aggregateRoots, List<EventProxy> events);

    /**
     * 当预备提交 事务的 时候执行
     *
     * @param unitOfWork  The Unit of Work of which the underlying transaction is being committed.
     * @param transaction the will commited transaction 
     */
    void onPrepareTransactionCommit(UnitOfWork unitOfWork, Object transaction);

    /**
     * 当 uow 清楚资源的 时候执行
     *
     * @param unitOfWork The Unit of Work being cleaned up
     */
    void onCleanup(UnitOfWork unitOfWork);

}
