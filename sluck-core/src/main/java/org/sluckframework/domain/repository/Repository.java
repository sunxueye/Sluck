package org.sluckframework.domain.repository;

import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 聚合仓储
 * 
 * @author sunxy
 * @time 2015年9月1日 上午12:09:30
 * @since 1.0
 */
public interface Repository<T extends AggregateRoot<ID>, ID extends Identifier<?>> {

    /**
     * 指定 聚合的标示符 和 版本加载聚合，如果版本为空，则不验证版本
     *
     * @param aggregateIdentifier The identifier of the aggregate to load
     * @param expectedVersion     The expected version of the aggregate to load, or <code>null</code> to indicate the
     *                            version should not be checked
     * @return The aggregate root with the given identifier.
     */
    T load(ID aggregateIdentifier, Long expectedVersion);

    /**
     * 根据标示符加载聚合
     *
     * @param aggregateIdentifier The identifier of the aggregate to load
     * @return The aggregate root with the given identifier.
     */
    T load(ID aggregateIdentifier);

    /**
     * 增加聚合到仓储中
     *
     * @param aggregate The aggregate to add to the repository.
     */
    void add(T aggregate);

}
