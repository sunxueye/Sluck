package org.sluckframework.implement.eventstore.jdbc.criteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 属性参数注册
 * 
 * @author sunxy
 * @time 2015年8月29日 下午11:53:03
 * @since 1.0
 */
public class ParameterRegistry {

	private final List<Object> parameters = new ArrayList<Object>();

	/**
	 * 注册给定的表达式 对应的参数
	 *
	 * @param expression The expression to parametrize in the query
	 * @return The placeholder to use query
	 */
	public String register(Object expression) {
		parameters.add(expression);
		return "?";
	}

	/**
	 * 返回注册的参数，不能修改
	 *
	 * @return a map containing the parameters
	 */
	public List<Object> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

}
