package org.sluckframework.domain.event.eventstore.query;


/** 
 * 领域事件在事件存储中的属性，这些属性通常都需要索引
 * 
 * @author sunxy
 * @time 2015年8月29日 下午4:20:33
 * @since 1.0
 */
public interface Property {
	
	/**
	 * 返回一个 比给定的 expression less than的条件实例 
	 * 
     * @param expression The expression to match against the property
     * @return a criteria instance describing a "less than" requirement.
     */
    Criteria lessThan(Object expression);

    /**
     * 返回一个 比给定的 expression  less than 或 equal 的条件
     * 
     * @param expression The expression to match against the property
     * @return a criteria instance describing a "less than or equals" requirement.
     */
    Criteria lessThanEquals(Object expression);

    /**
     * 返回一个比给定 expression greater than 的条件实例
     * 
     * @param expression The expression to match against the property
     * @return a criteria instance describing a "greater than" requirement.
     */
    Criteria greaterThan(Object expression);

    /**
     * 返回一个比给定 expression greater than 或  equal 的条件实例
     * 
     * @param expression The expression to match against the property
     * @return a criteria instance describing a "greater than or equals" requirement.
     */
    Criteria greaterThanEquals(Object expression);

    /**
     * 返回一个和给定 expression equal 的条件实例
     * 
     * @param expression The expression to match against the property
     * @return a criteria instance describing an "equals" requirement.
     */
    Criteria is(Object expression);

    /**
     * 返回一个和给定 expression not equal 的条件实例
     *
     * @param expression The expression to match against the property
     * @return a criteria instance describing a "not equals" requirement.
     */
    Criteria isNot(Object expression);

    /**
     * 返回一个和给定 expression in 的条件实例, expression 必须为集合
     * 
     * @param expression The expression to match against the property
     * @return a criteria instance describing a "is in" requirement.
     */
    Criteria in(Object expression);

    /**
     * 返回一个和给定 expression not in 的条件实例, expression 必须为集合
     *
     * @param expression The expression to match against the property
     * @return a criteria instance describing a "is not in" requirement.
     */
    Criteria notIn(Object expression);

}
