package org.sluckframework.common.lock;

import org.sluckframework.common.exception.SluckTransientException;

/**
 * 获取锁失败异常，但是可以重试
 * 
 * @author sunxy
 * @time 2015年9月1日 下午11:26:52
 * @since 1.0
 */
public class LockAcquisitionFailedException extends SluckTransientException {

	private static final long serialVersionUID = 5419285699102829561L;
	
    public LockAcquisitionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockAcquisitionFailedException(String message) {
        super(message);
    }

}
