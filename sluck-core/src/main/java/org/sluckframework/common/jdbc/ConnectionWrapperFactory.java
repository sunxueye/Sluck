package org.sluckframework.common.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * 创建 连接，可内置连接池
 * 
 * @author sunxy
 * @time 2015年8月30日 下午11:00:26
 * @since 1.0
 */
public abstract class ConnectionWrapperFactory {
	

    private ConnectionWrapperFactory() {
    }

    /**
     * 持有连接  返回 连接的代理，代理提供一些额外的功能
     *
     * @param connection       The connection to wrap
     * @param wrapperInterface The additional interface to implement
     * @param wrapperHandler   The implementation for the additional interface
     * @param closeHandler     The handler to redirect close invocations to
     * @return a wrapped Connection
     */
    public static <I> Connection wrap(final Connection connection, final Class<I> wrapperInterface,
                                      final I wrapperHandler,
                                      final ConnectionCloseHandler closeHandler) {
        return (Connection) Proxy.newProxyInstance(wrapperInterface.getClassLoader(),
                                                   new Class[]{Connection.class, wrapperInterface},
                                                   new InvocationHandler() {
                                                       @Override
                                                       public Object invoke(Object proxy, Method method, Object[] args)
                                                               throws Throwable {
                                                           if ("equals".equals(method.getName()) && args != null
                                                                   && args.length == 1) {
                                                               return proxy == args[0];
                                                           } else if ("hashCode".equals(
                                                                   method.getName()) && isEmpty(args)) {
                                                               return connection.hashCode();
                                                           } else if (method.getDeclaringClass().isAssignableFrom(
                                                                   wrapperInterface)) {
                                                               return method.invoke(wrapperHandler, args);
                                                           } else if ("close".equals(method.getName())
                                                                   && isEmpty(args)) {
                                                               closeHandler.close(connection);
                                                               return null;
                                                           } else if ("commit".equals(method.getName())
                                                                   && isEmpty(args)) {
                                                               closeHandler.commit(connection);
                                                               return null;
                                                           } else {
                                                               return method.invoke(connection, args);
                                                           }
                                                       }
                                                   }
        );
    }

    /**
     * 持有连接  返回 连接的代理，代理提供一些额外的功能
     *
     * @param connection   The connection to wrap
     * @param closeHandler The handler to redirect close invocations to
     * @return a wrapped Connection
     */
    public static Connection wrap(final Connection connection, final ConnectionCloseHandler closeHandler) {
        return (Connection) Proxy.newProxyInstance(closeHandler.getClass().getClassLoader(),
                                                   new Class[]{Connection.class},
                                                   new InvocationHandler() {
                                                       @Override
                                                       public Object invoke(Object proxy, Method method, Object[] args)
                                                               throws Throwable {
                                                           if ("equals".equals(method.getName()) && args != null
                                                                   && args.length == 1) {
                                                               return proxy == args[0];
                                                           } else if ("hashCode".equals(
                                                                   method.getName()) && isEmpty(args)) {
                                                               return connection.hashCode();
                                                           } else if ("close".equals(method.getName())
                                                                   && isEmpty(args)) {
                                                               closeHandler.close(connection);
                                                               return null;
                                                           } else if ("commit".equals(method.getName())
                                                                   && isEmpty(args)) {
                                                               closeHandler.commit(connection);
                                                               return null;
                                                           } else {
                                                               return method.invoke(connection, args);
                                                           }
                                                       }
                                                   }
        );
    }

    private static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 关闭连接
     */
    public interface ConnectionCloseHandler {

        /**
         * close
         *
         * @param connection the wrapped connection to close
         */
        void close(Connection connection);

        /**
         * Commits 事务
         *
         * @param connection the wrapped connection to commit
         */
        void commit(Connection connection) throws SQLException;
    }

    /**
     * 一个 简单适配器
     */
    public static class NoOpCloseHandler implements ConnectionCloseHandler {

        @Override
        public void close(Connection connection) {
        }

        @Override
        public void commit(Connection connection) throws SQLException {
            connection.commit();
        }
    }

}
