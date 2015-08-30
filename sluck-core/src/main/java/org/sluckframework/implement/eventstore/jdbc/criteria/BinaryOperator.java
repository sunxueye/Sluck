package org.sluckframework.implement.eventstore.jdbc.criteria;

/**
 * jdbc 仓储 对 binary 的操作
 * 
 * @author sunxy
 * @time 2015年8月29日 下午11:57:07
 * @since 1.0
 */
public class BinaryOperator extends JdbcCriteria {

	private final JdbcCriteria criteria1;
	private final JdbcCriteria criteria2;
	private final String operator;

	/**
	 * 根据两个 条件子句 初始化
	 *
	 * @param criteria1
	 * @param operator
	 * @param criteria2
	 */
	public BinaryOperator(JdbcCriteria criteria1, String operator,
			JdbcCriteria criteria2) {
		this.criteria1 = criteria1;
		this.operator = operator;
		this.criteria2 = criteria2;
	}

	@Override
	public void parse(String entryKey, StringBuilder whereClause, ParameterRegistry parameters) {
		whereClause.append("(");
		criteria1.parse(entryKey, whereClause, parameters);
		whereClause.append(") ").append(operator).append(" (");
		criteria2.parse(entryKey, whereClause, parameters);
		whereClause.append(")");
	}

}
