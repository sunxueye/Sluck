package org.sluckframework.common.jdbc;

import org.sluckframework.common.exception.SluckTransientException;

/**
 * jdbc事务异常，可通过重试恢复
 * 
 * @author sunxy
 * @time 2015年8月30日 下午11:07:58
 * @since 1.0
 */
public class JdbcTransactionException extends SluckTransientException {

	private static final long serialVersionUID = -2419774565490057431L;
	
	public JdbcTransactionException(String message, Throwable cause) {
	      super(message, cause);
	}

}
