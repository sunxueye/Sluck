package org.sluckframework.domain.event.aggregate;


/**
 * 聚合事件流
 * 
 * @author sunxy
 * @time 2015年8月28日 下午3:59:43	
 * @since 1.0
 */
public interface AggregateEventStream {
	
    /**
     * 流中是否还有下个聚合事件
     * 
     * @return
     */
    boolean hasNext();

    /**
     * 返回流中的下一个聚合事件，最好先用 hasNext()判断是否有下个事件再用该方法获取，
     * 使用该方法会使pointer指向 流中的下个事件的位置
     *
     * @return the next event in the stream.
     */
    @SuppressWarnings("rawtypes")
	AggregateEvent next();

    /**
     * 返回流中下个聚合事件，但是 pointer 指向不变
     * 
     * @return the next event in the stream.
     */
    @SuppressWarnings("rawtypes")
    AggregateEvent peek();

}
