package org.sluckframework.cqrs.commandhandling.disruptor;

import org.sluckframework.common.exception.SluckTransientException;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 聚合状态异常
 * 
 * @author sunxy
 * @time 2015年9月7日 下午5:31:49	
 * @since 1.0
 */
public class AggregateStateCorruptedException extends SluckTransientException {

	private static final long serialVersionUID = 4464768391622251309L;
	
	private final Identifier<?> aggregateIdentifier;

    public AggregateStateCorruptedException(Identifier<?> aggregateIdentifier, String message) {
        super(message);
        this.aggregateIdentifier = aggregateIdentifier;
    }

    public AggregateStateCorruptedException(Identifier<?> aggregateIdentifier, String message, Throwable cause) {
        super(message, cause);
        this.aggregateIdentifier = aggregateIdentifier;
    }

    public Identifier<?> getAggregateIdentifier() {
        return aggregateIdentifier;
    }

}
