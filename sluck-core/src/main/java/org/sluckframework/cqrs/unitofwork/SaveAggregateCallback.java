package org.sluckframework.cqrs.unitofwork;

import org.sluckframework.domain.aggregate.AggregateRoot;

/**
 * 使用 此 回调 函数 save 聚合
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:46:50
 * @since 1.0
 */
public interface SaveAggregateCallback<T extends AggregateRoot<?>> {
	
	/**
     * Invoked when the UnitOfWork wishes to store an aggregate.
     *
     * @param aggregate The aggregate to store
     */
    void save(T aggregate);

}
