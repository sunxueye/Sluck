package org.sluckframework.domain.repository;

import org.sluckframework.domain.identifier.Identifier;

/**
 * 聚合根版本 检测异常
 * 
 * @author sunxy
 * @time 2015年9月1日 上午12:36:15
 * @since 1.0
 */
public class ConflictingAggregateVersionException extends ConflictingModificationException {

	private static final long serialVersionUID = -7093548507942402636L;
	
    private final Identifier<?> aggregateIdentifier;
    private final long expectedVersion;
    private final long actualVersion;

    public ConflictingAggregateVersionException(Identifier<?> aggregateIdentifier,
                                                long expectedVersion, long actualVersion) {
        super(String.format("The version of aggregate [%s] was not as expected. "
                                    + "Expected [%s], but repository found [%s]",
                            aggregateIdentifier.toString(), expectedVersion, actualVersion));
        this.aggregateIdentifier = aggregateIdentifier;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public ConflictingAggregateVersionException(Identifier<?> aggregateIdentifier,
                                                long expectedVersion, long actualVersion, Throwable cause) {
        super(String.format("The version of aggregate [%s] was not as expected. "
                                    + "Expected [%s], but repository found [%s]",
                            aggregateIdentifier.toString(), expectedVersion, actualVersion),
              cause);
        this.aggregateIdentifier = aggregateIdentifier;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public Identifier<?> getAggregateIdentifier() {
        return aggregateIdentifier;
    }

    public long getExpectedVersion() {
        return expectedVersion;
    }

    public long getActualVersion() {
        return actualVersion;
    }

}
