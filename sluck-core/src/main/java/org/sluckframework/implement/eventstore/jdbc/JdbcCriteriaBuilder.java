package org.sluckframework.implement.eventstore.jdbc;

import org.sluckframework.domain.event.eventstore.query.CriteriaBuilder;
import org.sluckframework.domain.event.eventstore.query.Property;
import org.sluckframework.implement.eventstore.jdbc.criteria.JdbcProperty;

/**
 * jdbc 实现的 criteriaBuilder
 * 
 * @author sunxy
 * @time 2015年8月29日 下午11:45:19
 * @since 1.0
 */
public class JdbcCriteriaBuilder implements CriteriaBuilder {

	@Override
	public Property property(String propertyName) {
		return new JdbcProperty(propertyName);
	}

}
