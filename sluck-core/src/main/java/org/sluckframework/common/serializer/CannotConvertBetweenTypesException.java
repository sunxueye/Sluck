package org.sluckframework.common.serializer;

import org.sluckframework.common.exception.SluckConfigurationException;

/**
 * 转换异常
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:54:51
 * @since 1.0
 */
public class CannotConvertBetweenTypesException extends SluckConfigurationException {

	private static final long serialVersionUID = 6619723422532069621L;
	
    public CannotConvertBetweenTypesException(String message) {
        super(message);
    }

    public CannotConvertBetweenTypesException(String message, Throwable cause) {
        super(message, cause);
    }

}
