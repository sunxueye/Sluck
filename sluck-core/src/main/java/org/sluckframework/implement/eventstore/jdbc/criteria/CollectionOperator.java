package org.sluckframework.implement.eventstore.jdbc.criteria;

/**
 * jdbc 集合操作
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:08:38
 * @since 1.0
 */
public class CollectionOperator extends JdbcCriteria{

    private final JdbcProperty property;
    private final Object expression;
    private final String operator;

    /**
     * 根据jdbc属性 和 操作 和对应值 初始化
     *
     * @param property   The property to match
     * @param operator   The JPA operator to match the property against the expression
     * @param expression The expression to match against
     */
    public CollectionOperator(JdbcProperty property, String operator, Object expression) {
        this.property = property;
        this.expression = expression;
        this.operator = operator;
    }

    @Override
    public void parse(String entryKey, StringBuilder whereClause, ParameterRegistry parameters) {
        property.parse(entryKey, whereClause);
        whereClause.append(" ")
                   .append(operator)
                   .append(" ");
        if (expression instanceof JdbcProperty) {
            ((JdbcProperty) expression).parse(entryKey, whereClause);
        } else {
            whereClause.append("(")
                       .append(parameters.register(expression))
                       .append(")");
        }
    }

}
