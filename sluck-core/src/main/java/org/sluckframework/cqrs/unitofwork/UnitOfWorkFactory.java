package org.sluckframework.cqrs.unitofwork;

/**
 * create uow
 * 
 * @author sunxy
 * @time 2015年9月7日 上午9:48:14	
 * @since 1.0
 */
public interface UnitOfWorkFactory {
	
    UnitOfWork createUnitOfWork();

}
