package org.sluckframework.common.jdbc;

import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListenerAdapter;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * 持有 connectionProvider，使用的时候检查但却 UoW 是否已经持有连接，如不 则新建
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:31:04
 * @since 1.0
 */
public class UnitOfWorkAwareConnectionProviderWrapper implements ConnectionProvider {
	
	private static final String CONNECTION_RESOURCE_NAME = Connection.class.getName();

    private final ConnectionProvider delegate;
    private final boolean inherited;

    /**
     * 使用 指定  cp 初始化
     *
     * @param delegate The connection provider creating connections, when required
     */
    public UnitOfWorkAwareConnectionProviderWrapper(ConnectionProvider delegate) {
        this(delegate, true);
    }

    /**
     * 使用 cp 初始化， 并指定 资源是否能被 nested uow 使用
     *
     * @param delegate                  The connection provider creating connections, when required
     * @param attachAsInheritedResource whether or not nested Units of Work should inherit connections
     */
    public UnitOfWorkAwareConnectionProviderWrapper(ConnectionProvider delegate, boolean attachAsInheritedResource) {
        this.delegate = delegate;
        this.inherited = attachAsInheritedResource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (!CurrentUnitOfWork.isStarted()) {
            return delegate.getConnection();
        }

        UnitOfWork uow = CurrentUnitOfWork.get();
        Connection connection = uow.getResource(CONNECTION_RESOURCE_NAME);
        if (connection == null || connection.isClosed()) {
            final Connection delegateConnection = delegate.getConnection();
            connection = ConnectionWrapperFactory.wrap(delegateConnection,
                                                       UoWAttachedConnection.class,
                                                       new UoWAttachedConnectionImpl(delegateConnection),
                                                       new ConnectionWrapperFactory.NoOpCloseHandler());
            uow.attachResource(CONNECTION_RESOURCE_NAME, connection, inherited);
            uow.registerListener(new ConnectionManagingUnitOfWorkListenerAdapter());
        }
        return connection;
    }

    private interface UoWAttachedConnection {

        void forceClose();
    }

    private static class UoWAttachedConnectionImpl implements UoWAttachedConnection {

        private final Connection delegateConnection;

        public UoWAttachedConnectionImpl(Connection delegateConnection) {
            this.delegateConnection = delegateConnection;
        }

        @Override
        public void forceClose() {
            JdbcUtils.closeQuietly(delegateConnection);
        }
    }

    private static class ConnectionManagingUnitOfWorkListenerAdapter extends UnitOfWorkListenerAdapter {

        @Override
        public void afterCommit(UnitOfWork unitOfWork) {
            if (!unitOfWork.isTransactional()) {
                onPrepareTransactionCommit(unitOfWork, null);
            }
        }

        @Override
        public void onPrepareTransactionCommit(UnitOfWork unitOfWork, Object transaction) {
            Connection connection = unitOfWork.getResource(CONNECTION_RESOURCE_NAME);
            try {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            } catch (SQLException e) {
                throw new JdbcTransactionException("Unable to commit transaction", e);
            }
        }

        @Override
        public void onCleanup(UnitOfWork unitOfWork) {
            Connection connection = unitOfWork.getResource(CONNECTION_RESOURCE_NAME);
            JdbcUtils.closeQuietly(connection);
            if (connection instanceof UoWAttachedConnection) {
                ((UoWAttachedConnection) connection).forceClose();
            }
        }

        @Override
        public void onRollback(UnitOfWork unitOfWork, Throwable failureCause) {
            Connection connection = unitOfWork.getResource(CONNECTION_RESOURCE_NAME);
            try {
                if (!connection.isClosed() && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                throw new JdbcTransactionException("Unable to rollback transaction", e);
            }
        }
    }

}
