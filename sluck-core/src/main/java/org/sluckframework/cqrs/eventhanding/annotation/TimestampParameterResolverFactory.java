package org.sluckframework.cqrs.eventhanding.annotation;

import org.joda.time.DateTime;
import org.sluckframework.common.annotation.Priority;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.ParameterResolver;


/**
 * Timestamp解析器
 * 
 * @author sunxy
 * @time 2015年9月6日 下午8:06:17
 * @since 1.0
 */
@Priority(Priority.HIGH)
public class TimestampParameterResolverFactory  
extends AbstractAnnotatedParameterResolverFactory<Timestamp, DateTime> {

    private final ParameterResolver<DateTime> resolver;

    public TimestampParameterResolverFactory() {
        super(Timestamp.class, DateTime.class);
        resolver = new TimestampParameterResolver();
    }

    @Override
    protected ParameterResolver<DateTime> getResolver() {
        return resolver;
    }

    @SuppressWarnings("rawtypes")
    static class TimestampParameterResolver implements ParameterResolver<DateTime> {

        @Override
        public DateTime resolveParameterValue(EventProxy message) {
        	return message.occurredOn();
        }

		@Override
        public boolean matches(EventProxy message) {
            return true;
        }
    }
}
