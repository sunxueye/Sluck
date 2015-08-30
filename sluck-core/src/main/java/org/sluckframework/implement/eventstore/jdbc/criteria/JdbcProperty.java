package org.sluckframework.implement.eventstore.jdbc.criteria;

import org.sluckframework.domain.event.eventstore.query.Criteria;
import org.sluckframework.domain.event.eventstore.query.Property;

/**
 * jdbc 的Property 实现
 * 
 * @author sunxy
 * @time 2015年8月29日 下午11:46:45
 * @since 1.0
 */
public class JdbcProperty implements Property {

	private final String propertyName;

	/**
	 * 根据属性名称 初始化
	 *
	 * @param propertyName The name of the property
	 */
	public JdbcProperty(String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public JdbcCriteria lessThan(Object expression) {
		return new SimpleOperator(this, "<", expression);
	}

	@Override
	public JdbcCriteria lessThanEquals(Object expression) {
		return new SimpleOperator(this, "<=", expression);
	}

	@Override
	public JdbcCriteria greaterThan(Object expression) {
		return new SimpleOperator(this, ">", expression);
	}

	@Override
	public JdbcCriteria greaterThanEquals(Object expression) {
		return new SimpleOperator(this, ">=", expression);
	}

	@Override
	public JdbcCriteria is(Object expression) {
		return new Equals(this, expression);
	}

	@Override
	public JdbcCriteria isNot(Object expression) {
		return new NotEquals(this, expression);
	}

	@Override
	public Criteria in(Object expression) {
		return new CollectionOperator(this, "IN", expression);
	}

	@Override
	public Criteria notIn(Object expression) {
		return new CollectionOperator(this, "NOT IN", expression);
	}

	/**
	 * 属性解析 为Sql
	 *
	 * @param entryKey entry key
	 * @param stringBuilder The builder to append the expression to
	 */
	public void parse(String entryKey, StringBuilder stringBuilder) {
		if (entryKey != null && entryKey.length() > 0) {
			stringBuilder.append(entryKey).append(".");
		}
		stringBuilder.append(propertyName);
	}
}
