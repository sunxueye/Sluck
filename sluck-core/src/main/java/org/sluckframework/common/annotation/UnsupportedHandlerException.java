package org.sluckframework.common.annotation;

import java.lang.reflect.Member;

import org.sluckframework.common.exception.SluckConfigurationException;

/**
 * 不支持的方法处理 类型
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:25:28	
 * @since 1.0
 */
public class UnsupportedHandlerException extends SluckConfigurationException {

	private static final long serialVersionUID = 2129452836873824836L;
	
	private final Member violatingMethod;
	
	public UnsupportedHandlerException(String message, Member violatingMethod) {
        super(message);
        this.violatingMethod = violatingMethod;
    }

    public Member getViolatingMethod() {
        return violatingMethod;
    }

}
