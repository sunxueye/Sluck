package org.sluckframework.common.lock;
/**
 * 发生死锁异常，但是可以重试，因为对象锁应用程序可控
 * 
 * @author sunxy
 * @time 2015年9月1日 下午11:28:23
 * @since 1.0
 */
public class DeadlockException extends IllegalLockUsageException {

	private static final long serialVersionUID = 8595412699462829794L;
	
	public DeadlockException(String message) {
	     super(message);
	}

}
