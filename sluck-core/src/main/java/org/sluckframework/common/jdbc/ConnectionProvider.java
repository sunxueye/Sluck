package org.sluckframework.common.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 提供 jdbc 连接
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:13:48
 * @since 1.0
 */
public interface ConnectionProvider {
	
    Connection getConnection() throws SQLException;
}
