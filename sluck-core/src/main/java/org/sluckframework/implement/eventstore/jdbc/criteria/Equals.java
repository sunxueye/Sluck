package org.sluckframework.implement.eventstore.jdbc.criteria;

/**
 * jdbc equals
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:04:28
 * @since 1.0
 */
public class Equals extends JdbcCriteria{
	
	private final JdbcProperty propertyName;
	private final Object expression;

	/**
	 * 根据 property 和 expression 初始化
	 *
	 * @param property The property to match
	 * @param expression The expression to match against
	 */
	public Equals(JdbcProperty property, Object expression) {
		this.propertyName = property;
		this.expression = expression;
	}

	@Override
	public void parse(String entryKey, StringBuilder whereClause,
			ParameterRegistry parameters) {
		propertyName.parse(entryKey, whereClause);
		if (expression == null) {
			whereClause.append(" IS NULL");
		} else {
			whereClause.append(" = ");
			if (expression instanceof JdbcProperty) {
				((JdbcProperty) expression).parse(entryKey, whereClause);
			} else {
				whereClause.append(parameters.register(expression));
			}
		}
	}

}
