package org.sluckframework.cqrs.unitofwork;
/**
 * uow 的 事务管理器
 * 
 * @author sunxy
 * @time 2015年9月2日 下午2:57:46	
 * @since 1.0
 */
public interface TransactionManager<T> {

    /**
     * 开始事务，并返回能代表当期事务状态 和 标识 的对象，  执行 提交和回滚事务需要这个标识符
     * 当事务成功创建时，这个标识符不能为空
     * 
     * @return The object representing the transaction status
     */
    T startTransaction();

    /**
     * 根据 事务标识 提交事务
     *
     * @param transactionStatus The status object provided by {@link #startTransaction()}.
     */
    void commitTransaction(T transactionStatus);

    /**
     * 根据 事务标识 回滚事务
     * 
     * @param transactionStatus The status object provided by {@link #startTransaction()}.
     */
    void rollbackTransaction(T transactionStatus);

}
