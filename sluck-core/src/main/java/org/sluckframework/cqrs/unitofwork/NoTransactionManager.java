package org.sluckframework.cqrs.unitofwork;
/**
 * 无事务的事务管理器
 * 
 * @author sunxy
 * @time 2015年9月6日 上午12:11:44
 * @since 1.0
 */
public class NoTransactionManager implements TransactionManager<String> {
	
    private static final String STATUS = "NoTransactionStatus";

    @Override
    public String startTransaction() {
        return STATUS;
    }

    @Override
    public void commitTransaction(String transactionStatus) {
    }

    @Override
    public void rollbackTransaction(String transaction) {
    }

}
