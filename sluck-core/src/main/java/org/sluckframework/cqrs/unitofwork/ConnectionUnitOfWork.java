package org.sluckframework.cqrs.unitofwork;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带有 jdbc连接 的 uow
 * 
 * @author sunxy
 * @time 2015年11月7日 下午11:34:08
 * @since 1.0
 */
public class ConnectionUnitOfWork extends DefaultUnitOfWork {
	 private static final Logger logger = LoggerFactory.getLogger(ConnectionUnitOfWork.class);
	 
	 private Connection connection;

}
