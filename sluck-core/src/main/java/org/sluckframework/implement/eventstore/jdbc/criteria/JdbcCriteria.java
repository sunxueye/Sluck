package org.sluckframework.implement.eventstore.jdbc.criteria;


import org.sluckframework.domain.event.eventstore.query.Criteria;


/**
 * jdbc 的 criteria 的抽象基类
 * @author sunxy
 * @time 2015年8月29日 下午11:48:15
 * @since 1.0
 */
public abstract class JdbcCriteria implements Criteria {
	
	@Override
	public JdbcCriteria and(Criteria criteria) {
		return new BinaryOperator(this, "AND", (JdbcCriteria) criteria);
	}

	@Override
	public JdbcCriteria or(Criteria criteria) {
		return new BinaryOperator(this, "OR", (JdbcCriteria) criteria);
	}

	/**
	 * 对条件解析
	 *
	 * @param entryKey The variable assigned to the entry in the whereClause
	 * @param whereClause The buffer to write the where clause to.
	 * @param parameters The registry where parameters and assigned values can be registered.
	 */
	public abstract void parse(String entryKey, StringBuilder whereClause,
			ParameterRegistry parameters);

}
