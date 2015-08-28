package com.sluckframework.domain.aggregate;

import java.io.Serializable;

import com.sluckframework.domain.event.aggregate.AggregateEventStream;
import com.sluckframework.domain.identifier.Identifier;

/**
 * 领域模型中的聚合根，所有聚合间的操作都通过聚合根来联系执行
 * 
 * @author sunxy
 * @time 2015年8月28日 上午11:28:17	
 * @since 1.0
 */
public interface AggregateRoot<ID extends Identifier<?>> extends Serializable{
	
    ID getIdentifier();

    /**
     * 提交聚合产生的事件，并clear 容器
     */
    void commitEvents();

    /**
     * 获取未提交的事件的数量
     * 
     * @return count
     */
    int getUncommittedEventCount();

    /**
     * 获取未提交的事件的事件流
     * 
     * @return eventStream
     */
    AggregateEventStream getUncommittedEvents();

    /**
     * 返回当前聚合的版本，如果聚合是新建的则为Null,当聚合被修改或者保存的时候，version至少增加1
     * 通常也可以将最后提交事件的sequence当做版本号
     *
     * @return the current version number of this aggregate, or null if no events were ever committed
     */
    Long getVersion();

    /**
     * 判断当前聚合是被标记删除，如果是则仓储会在适当的时间删除这个聚合，且从仓储中获取该聚合时，应该返回null
     *
     * @return true if this aggregate was marked as deleted, otherwise false.
     */
    boolean isDeleted();

    /**
     * 当聚合注册 需要发布的事件的时候， 调用这个回调函数， 当聚合提交时，被清空
     *
     * @param eventRegistrationCallback the callback to notify when an event is registered
     */
    void addEventRegistrationCallback(EventRegistrationCallback eventRegistrationCallback);

}
