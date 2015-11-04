package org.sluckframework.cqrs.saga;

import org.sluckframework.domain.event.EventProxy;

/**
 * 流程管理器,由于ES的EDA驱动一次只能修改一个聚合,如果涉及到多个聚合的修改,需要一个流程管理器,就是Saga
 *
 * Author: sunxy
 * Created: 2015-09-11 19:47
 * Since: 1.0
 */
public interface Saga {

    /**
     * 返回Saga的标示符
     *
     * @return the unique identifier of this saga
     */
    String getSagaIdentifier();

    /**
     * Returns a view on the Association Values for this saga instance. The returned instance is mutable.
     *
     * @return a view on the Association Values for this saga instance
     */
    AssociationValues getAssociationValues();

    /**
     * 增加关联值
     *
     * @param property target associationValue
     */
    void associateWith(AssociationValue property);

    /**
     * 处理事件
     *
     * @param event the event to handle
     */
    void handle(EventProxy<?> event);

    /**
     * 是否处于激活状态
     *
     * @return <code>true</code> if this saga is active, <code>false</code> otherwise.
     */
    boolean isActive();

    /**
     * 重置 所有关联值
     */
    void reSetAssociationValues();

}
