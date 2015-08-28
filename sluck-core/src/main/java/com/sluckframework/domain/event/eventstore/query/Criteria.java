package com.sluckframework.domain.event.eventstore.query;


/**
 * 查询条件，可以使使用 and or 方法和其他条件组合
 * 
 * @author sunxy
 * @time 2015年8月29日 上午2:04:51
 * @since 1.0
 */
public interface Criteria {
	
	/**
     * And &&
     *
     * @param criteria The criteria that must match
     * @return a criteria instance that matches if both this and criteria match
     */
    Criteria and(Criteria criteria);

    /**
     * OR ||
     * 
     * @param criteria The criteria that must match if <code>this</code> doesn't match
     * @return a criteria instance that matches if this or the given criteria match
     */
    Criteria or(Criteria criteria);

}
