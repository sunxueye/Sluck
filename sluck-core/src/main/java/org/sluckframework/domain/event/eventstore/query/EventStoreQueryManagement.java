package org.sluckframework.domain.event.eventstore.query;

import org.sluckframework.domain.event.eventstore.EventVisitor;


/**
 * 对事件仓储查询的管理，当应用程序版本变迁的时候，对新旧查询脚本进行适配管理
 * 
 * @author sunxy
 * @time 2015年8月29日 下午4:34:33
 * @since 1.0 
 */
public interface EventStoreQueryManagement {
	
	/**
	 * 从仓储读取所有事件，并使用visitor来遍历这些事件，单个聚合的事件将会被它们的sequence number排序
	 * 
     * @param visitor The visitor the receives each loaded event
     */
    void visitEvents(EventVisitor visitor);

    /**
     * 读取所有满足条件的事件，并使用 visitor遍历， 单个聚合的事件根据 sequnce 排序
     * 
     * @param criteria The criteria describing the events to select
     * @param visitor  The visitor the receives each loaded event
     * @see #newCriteriaBuilder()
     */
    void visitEvents(Criteria criteria, EventVisitor visitor);

    /**
     * 构造条件 builder
     *
     * @return a builder to create Criteria for this Event Store.
     *
     * @see #visitEvents(Criteria, org.axonframework.eventstore.EventVisitor)
     */
    CriteriaBuilder newCriteriaBuilder();

}
