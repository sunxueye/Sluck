package org.sluckframework.common.jdbc;
/**
 * 用于找出 重复主键 异常的 解析器
 * 
 * @author sunxy
 * @time 2015年8月30日 上午1:52:47
 * @since 1.0
 */
public interface PersistenceExceptionResolver {

    /**
     * 判断异常是否 由于 重复主键 造成
     *
     * @param exception The exception to evaluate
     * @return <code>true</code> if the given exception represents a Duplicate Key Violation, <code>false</code>
     *         otherwise.
     */
    boolean isDuplicateKeyViolation(Exception exception);

}
