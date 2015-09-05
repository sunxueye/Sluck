package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.domain.identifier.Identifier;

/**
 * 聚合快照生成，用于生成聚合快照事件
 * 
 * @author sunxy
 * @time 2015年9月6日 上午12:00:02
 * @since 1.0
 */
public interface Snapshotter {
	
	/**
	 * 计划生成快照，可能是同步也可以是异常生成快照
	 * 
     * @param typeIdentifier      the type of the aggregate to take the snapshot for
     * @param aggregateIdentifier The identifier of the aggregate to take the snapshot for
     */
    void scheduleSnapshot(String typeIdentifier, Identifier<?> aggregateIdentifier);

}
