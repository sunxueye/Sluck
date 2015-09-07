package org.sluckframework.cqrs.commandhandling;
/**
 * 当 uow 回滚时候 的回滚设置
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:05:54	
 * @since 1.0
 */
public interface RollbackConfiguration {

    /**
     * 当出现指定异常的时候 回滚
     *
     * @param throwable the Throwable to evaluate
     * @return <code>true</code> if the UnitOfWork should be rolled back, otherwise <code>false</code>
     */
    boolean rollBackOn(Throwable throwable);

}
