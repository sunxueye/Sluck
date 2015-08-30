package org.sluckframework.implement.eventstore.jdbc.criteria;

/**
 * 简单的 jdbcCriteria的实现 
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:00:57
 * @since 1.0
 */
public class SimpleOperator extends JdbcCriteria {
	
	private final JdbcProperty propertyName;
    private final String operator;
    private final Object expression;

    public SimpleOperator(JdbcProperty property, String operator, Object expression) {
        this.propertyName = property;
        this.operator = operator;
        this.expression = expression;
    }

    @Override
    public void parse(String entryKey, StringBuilder whereClause, ParameterRegistry parameters) {
        propertyName.parse(entryKey, whereClause);
        whereClause.append(" ")
                   .append(operator)
                   .append(" ");
        if (expression instanceof JdbcProperty) {
            ((JdbcProperty) expression).parse(entryKey, whereClause);
        } else {
            whereClause.append(parameters.register(expression));
        }
    }

}
