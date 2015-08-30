package org.sluckframework.implement.eventstore.jdbc.criteria;

/**
 * jdbc not equals
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:07:12
 * @since 1.0
 */
public class NotEquals extends JdbcCriteria{

    private final JdbcProperty property;
    private final Object expression;

    /**
     * 使用 属性 proterty 和 对应的 值 初始化
     *
     * @param property   The property to match
     * @param expression The expression to match against. May be <code>null</code>.
     */
    public NotEquals(JdbcProperty property, Object expression) {
        this.property = property;
        this.expression = expression;
    }

    @Override
    public void parse(String entryKey, StringBuilder whereClause, ParameterRegistry parameters) {
        property.parse(entryKey, whereClause);
        if (expression == null) {
            whereClause.append(" IS NOT NULL");
        } else {
            whereClause.append(" <> ");
            if (expression instanceof JdbcProperty) {
                ((JdbcProperty) expression).parse(entryKey, whereClause);
            } else {
                whereClause.append(parameters.register(expression));
            }
        }
    }

}
