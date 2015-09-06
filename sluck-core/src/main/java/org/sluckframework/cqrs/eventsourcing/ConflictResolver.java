package org.sluckframework.cqrs.eventsourcing;

import java.util.List;

import org.sluckframework.domain.event.aggregate.AggregateEvent;

/**
 * 聚合的 并发冲突解决
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:53:20	
 * @since 1.0
 */
public interface ConflictResolver {

    /**
     * 检查 申请 变动列表事件 和已 提交的事件 之间的并发冲突
     *
     * @param appliedChanges   The list of the changes applied to the aggregate
     * @param committedChanges The list of events that have been previously applied, but were unexpected by the command
     *                         handler
     */
    @SuppressWarnings("rawtypes")
	void resolveConflicts(List<AggregateEvent> appliedChanges, List<AggregateEvent> committedChanges);

}
