package org.sluckframework.common.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * 数据源
 * 
 * @author sunxy
 * @time 2015年8月30日 下午11:10:07
 * @since 1.0
 */
public class DataSourceConnectionProvider implements ConnectionProvider {

	private final DataSource dataSource;

	/**
	 * 使用指定 数据源 初始化
	 * @param dataSource
	 */
	public DataSourceConnectionProvider(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

}
