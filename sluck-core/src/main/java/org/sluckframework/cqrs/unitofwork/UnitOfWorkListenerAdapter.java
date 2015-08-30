package org.sluckframework.cqrs.unitofwork;

import java.util.List;
import java.util.Set;

import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.event.EventProxy;

/**
 * 一个简单的 uow 监听器的 适配器
 * 
 * @author sunxy
 * @time 2015年8月30日 下午11:05:57
 * @since 1.0
 */
public abstract class UnitOfWorkListenerAdapter implements UnitOfWorkListener {

	@Override
	public void afterCommit(UnitOfWork unitOfWork) {
	}

	@Override
	public void onRollback(UnitOfWork unitOfWork, Throwable failureCause) {
	}

	@Override
	public <T> EventProxy<T> onEventRegistered(UnitOfWork unitOfWork,
			EventProxy<T> event) {
		return event;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onPrepareCommit(UnitOfWork unitOfWork,
			Set<AggregateRoot> aggregateRoots, List<EventProxy> events) {
	}

	@Override
	public void onPrepareTransactionCommit(UnitOfWork unitOfWork,
			Object transaction) {
	}

	@Override
	public void onCleanup(UnitOfWork unitOfWork) {
	}

}
