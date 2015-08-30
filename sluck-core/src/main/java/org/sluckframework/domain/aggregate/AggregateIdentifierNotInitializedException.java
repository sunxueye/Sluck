package org.sluckframework.domain.aggregate;

import org.sluckframework.common.exception.SluckNonTransientException;

/**
 * 聚合的标识符未初始化异常
 * 
 * @author sunxy
 * @time 2015年8月28日 下午5:42:37	
 * @since 1.0
 */
public class AggregateIdentifierNotInitializedException extends
		SluckNonTransientException {

	private static final long serialVersionUID = 2950262486644717540L;
	
	public AggregateIdentifierNotInitializedException(String message) {
		super(message);
	}

}
