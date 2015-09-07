package org.sluckframework.cqrs.commandhandling;
/**
 * 无法检测的异常回滚，运行期异常 和 非exception子类 进行回滚  
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:07:37	
 * @since 1.0
 */
public class RollbackOnUncheckedExceptionConfiguration implements RollbackConfiguration {

	@Override
    public boolean rollBackOn(Throwable throwable) {
        return !(throwable instanceof Exception) || throwable instanceof RuntimeException;
    }

}
