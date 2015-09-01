package org.sluckframework.common.lock;

import org.sluckframework.common.exception.SluckNonTransientException;

/**
 * 获取锁时候发生的非法操作异常，不能重试
 * 
 * @author sunxy
 * @time 2015年9月1日 下午11:24:19
 * @since 1.0
 */
public class IllegalLockUsageException extends SluckNonTransientException {

	private static final long serialVersionUID = 5672826975838482553L;

	public IllegalLockUsageException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalLockUsageException(String message) {
		super(message);
	}

}
