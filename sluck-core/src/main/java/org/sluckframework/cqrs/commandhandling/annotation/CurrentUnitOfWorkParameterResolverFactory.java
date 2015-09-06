package org.sluckframework.cqrs.commandhandling.annotation;

import java.lang.annotation.Annotation;

import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.annotation.Priority;
import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.ParameterResolver;

/**
 * @author sunxy
 * @time 2015年9月6日 下午8:15:23
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Priority.FIRST)
public class CurrentUnitOfWorkParameterResolverFactory implements ParameterResolverFactory, ParameterResolver {

    @Override
    public ParameterResolver createInstance(Annotation[] memberAnnotations, Class<?> parameterType,
                                            Annotation[] parameterAnnotations) {
        if (UnitOfWork.class.isAssignableFrom(parameterType)) {
            return this;
        }
        return null;
    }

    @Override
    public Object resolveParameterValue(EventProxy message) {
        return CurrentUnitOfWork.get();
    }

	@Override
    public boolean matches(EventProxy message) {
        return Command.class.isInstance(message) && CurrentUnitOfWork.isStarted();
    }
}
