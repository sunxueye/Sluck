package com.sluckframework.common.exception;
/**
 * 非瞬态异常，重试不能解决的异常
 * 
 * @author sunxy
 * @time 2015年8月28日 下午2:39:12	
 * @since 1.0
 */
public class SluckNonTransientException extends SluckException {

	private static final long serialVersionUID = 6197177128292708859L;
	
	public static boolean isCauseOf(Throwable throwable) {
        return throwable != null
                && (throwable instanceof SluckNonTransientException || isCauseOf(throwable.getCause()));
    }

	public SluckNonTransientException(String message, Throwable cause) {
		super(message, cause);
	}

	public SluckNonTransientException(String message) {
		super(message);
	}
	
	

}
