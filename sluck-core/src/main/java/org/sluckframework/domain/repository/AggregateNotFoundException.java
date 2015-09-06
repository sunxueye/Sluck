package org.sluckframework.domain.repository;

import org.sluckframework.common.exception.SluckNonTransientException;
import org.sluckframework.domain.identifier.Identifier;

/**
 * @author sunxy
 * @time 2015年9月6日 下午5:12:12	
 * @since 1.0
 */
public class AggregateNotFoundException extends SluckNonTransientException {

	private static final long serialVersionUID = 590604501041769757L;
	
    private final Identifier<?> aggregateIdentifier;

    public AggregateNotFoundException(Identifier<?> aggregateIdentifier, String message) {
        super(message);
        this.aggregateIdentifier = aggregateIdentifier;
    }

    public AggregateNotFoundException(Identifier<?> aggregateIdentifier, String message, Throwable cause) {
        super(message, cause);
        this.aggregateIdentifier = aggregateIdentifier;
    }

    public Object getAggregateIdentifier() {
        return aggregateIdentifier;
    }

}
