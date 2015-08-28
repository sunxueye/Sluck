package com.sluckframework.common.exception;

import java.util.Collection;

/**
 * java 模拟断言 检测规则
 * @author sunxy
 * @time 2015年8月28日 下午12:51:23	
 * @since 1.0
 */
public class Assert {
	
	 private Assert() {
	        // utility class
	    }

	    /**判断是否给定boolean,不是输出错误信息
	     * @param state
	     * @param message
	     */
	    public static void state(boolean state, String message) {
	        if (!state) {
	            throw new IllegalStateException(message);
	        }
	    }

	    
	    /**判断给定的表达式是否为真
	     * @param expression
	     * @param message
	     */
	    public static void isTrue(boolean expression, String message) {
	        if (!expression) {
	            throw new IllegalArgumentException(message);
	        }
	    }

	    /**判断给定的表达式是否为假
	     * @param expression
	     * @param message
	     */
	    public static void isFalse(boolean expression, String message) {
	        if (expression) {
	            throw new IllegalArgumentException(message);
	        }
	    }

	    
	    /**判断给定的object不为null
	     * @param value
	     * @param message
	     */
	    public static void notNull(Object value, String message) {
	        isTrue(value != null, message);
	    }

	    /**判断给定的object不为empty
	     * @param value
	     * @param message
	     */
	    public static void notEmpty(String value, String message) {
	        notNull(value, message);
	        isFalse(value.isEmpty(), message);
	    }
	    
	    /**判断指定的集合不为空
	     * @param collection
	     * @param message
	     */
	    public static <T> void notEmpty(Collection<T> collection, String message) {
	    	notNull(collection, message);
	    	isFalse(collection.isEmpty(), message);
	    }

}
