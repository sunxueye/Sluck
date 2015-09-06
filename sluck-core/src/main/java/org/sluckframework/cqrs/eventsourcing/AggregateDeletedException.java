package org.sluckframework.cqrs.eventsourcing;

import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.AggregateNotFoundException;


/**
 * @author sunxy
 * @time 2015年9月6日 下午5:13:32	
 * @since 1.0
 */
public class AggregateDeletedException extends AggregateNotFoundException{

	private static final long serialVersionUID = 2682919354489177771L;
	
	public AggregateDeletedException(Identifier<?> aggregateIdentifier, String message) {
        super(aggregateIdentifier, message);
    }

    public AggregateDeletedException(Identifier<?> aggregateIdentifier) {
        this(aggregateIdentifier,
             String.format("Aggregate with identifier [%s] not found. It has been deleted.", aggregateIdentifier));
    }

}
