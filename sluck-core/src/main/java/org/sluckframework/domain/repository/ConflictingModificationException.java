package org.sluckframework.domain.repository;

import org.sluckframework.common.exception.SluckNonTransientException;

/**
 * 同步聚合修改异常
 * 
 * @author sunxy
 * @time 2015年9月1日 上午12:35:12
 * @since 1.0
 */
public class ConflictingModificationException extends SluckNonTransientException {

	private static final long serialVersionUID = 4992284847659865497L;
	
    public ConflictingModificationException(String message) {
        super(message);
    }

    public ConflictingModificationException(String message, Throwable cause) {
        super(message, cause);
    }

}
