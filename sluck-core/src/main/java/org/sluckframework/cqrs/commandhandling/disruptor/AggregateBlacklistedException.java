package org.sluckframework.cqrs.commandhandling.disruptor;

import org.sluckframework.domain.identifier.Identifier;

/**
 * 聚合名单异常
 * 
 * @author sunxy
 * @time 2015年9月7日 下午5:35:07	
 * @since 1.0
 */
public class AggregateBlacklistedException extends AggregateStateCorruptedException {

	private static final long serialVersionUID = 6492249784714556465L;
	
	public AggregateBlacklistedException(Identifier<?> aggregateIdentifier, String message, Throwable cause) {
        super(aggregateIdentifier, message, cause);
    }

}
