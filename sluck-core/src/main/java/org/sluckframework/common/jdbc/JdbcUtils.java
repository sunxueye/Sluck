package org.sluckframework.common.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * jdbc 工具类 用于关闭指定资源
 *
 * @author sunxy
 * @since 1.0
 */
public class JdbcUtils {

    /**
     * 关闭 给定的 rs
     *
     * @param resultSet The resource to close
     */
    public static void closeQuietly(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * 关闭给定的 statement
     *
     * @param statement The resource to close
     */
    public static void closeQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * 关闭给定的连接
     *
     * @param connection The resource to close
     */
    public static void closeQuietly(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            //ignore
        }
    }
}
