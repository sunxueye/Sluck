package org.sluckframework.cqrs.eventhanding.annotation;

import org.sluckframework.common.annotation.Priority;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.ParameterResolver;
import org.sluckframework.domain.event.aggregate.AggregateEvent;

/**
 * 聚合 事件的 sequenceNumber解析器
 * 
 * @author sunxy
 * @time 2015年9月6日 下午8:12:16
 * @since 1.0
 */
@Priority(Priority.HIGH)
public final class SequenceNumberParameterResolverFactory extends
        AbstractAnnotatedParameterResolverFactory<SequenceNumber, Long> {

    private final ParameterResolver<Long> resolver;

    public SequenceNumberParameterResolverFactory() {
        super(SequenceNumber.class, Long.class);
        resolver = new SequenceNumberParameterResolver();
    }

    @Override
    protected ParameterResolver<Long> getResolver() {
        return resolver;
    }

    /**
     * ParameterResolver that resolves SequenceNumber parameters
     */
    @SuppressWarnings("rawtypes")
    static class SequenceNumberParameterResolver implements ParameterResolver<Long> {

        @Override
        public Long resolveParameterValue(EventProxy message) {
            if (message instanceof AggregateEvent) {
                return ((AggregateEvent) message).getSequenceNumber();
            }
            return null;
        }

		@Override
        public boolean matches(EventProxy message) {
            return message instanceof AggregateEvent;
        }
    }
}
