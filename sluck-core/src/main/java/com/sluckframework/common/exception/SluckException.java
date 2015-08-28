package com.sluckframework.common.exception;
/**
 * sluck总的运行期异常 基类
 * 
 * @author sunxy
 * @time 2015年8月28日 下午2:29:42	
 * @since 1.0
 */
public abstract class SluckException extends RuntimeException{

	private static final long serialVersionUID = 4573495502571128652L;
	
    public SluckException(String message) {
        super(message);
    }

    public SluckException(String message, Throwable cause) {
        super(message, cause);
    }

}
