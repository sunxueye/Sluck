package org.sluckframework.implement.eventstore.jdbc;

import org.sluckframework.common.jdbc.PersistenceExceptionResolver;

/**
 * jdbc sql 解析 持久化异常
 * @author sunxy
 * @time 2015年8月30日 上午1:56:39
 * @since 1.0
 */
public class JdbcSQLErrorCodesResolver implements PersistenceExceptionResolver {
	
	@Override
    public boolean isDuplicateKeyViolation(Exception exception) {
        return causeIsEntityExistsException(exception);
    }

    private boolean causeIsEntityExistsException(Throwable exception) {
        return exception instanceof java.sql.SQLIntegrityConstraintViolationException
                || (exception.getCause() != null && causeIsEntityExistsException(exception.getCause()));
    }
}
