package org.sluckframework.cqrs.commandhandling;
/**
 * 所有异常都回滚
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:27:01	
 * @since 1.0
 */
public class RollbackOnAllExceptionsConfiguration implements
		RollbackConfiguration {

	@Override
	public boolean rollBackOn(Throwable throwable) {
		return true;
	}

}
