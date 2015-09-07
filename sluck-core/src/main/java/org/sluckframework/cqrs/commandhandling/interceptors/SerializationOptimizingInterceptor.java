package org.sluckframework.cqrs.commandhandling.interceptors;

import org.sluckframework.common.serializer.SerializationAwareAggregateEvent;
import org.sluckframework.common.serializer.SerializationAwareEventProxy;
import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandHandlerInterceptor;
import org.sluckframework.cqrs.commandhandling.InterceptorChain;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListenerAdapter;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.event.aggregate.AggregateEvent;

/**
 * 拦截器执行 注册一个 uow监听器 监听 将事件 变为 持有原有事件的 可序列化事件，使用这样机制可以让 当存储事件 和 发布事件 的时候 性能最大化 
 * 
 * @author sunxy
 * @time 2015年9月7日 下午11:18:09
 * @since 1.0
 */
public class SerializationOptimizingInterceptor implements CommandHandlerInterceptor {

    private final SerializationOptimizingListener listener = new SerializationOptimizingListener();

    @Override
    public Object handle(Command<?> Command, UnitOfWork unitOfWork,
                         InterceptorChain interceptorChain)
            throws Throwable {
        unitOfWork.registerListener(listener);
        return interceptorChain.proceed();
    }

    private static final class SerializationOptimizingListener extends UnitOfWorkListenerAdapter {

        @SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
        public <T> EventProxy<T> onEventRegistered(UnitOfWork unitOfWork, EventProxy<T> event) {
            if (event instanceof AggregateEvent) {
                return SerializationAwareAggregateEvent.wrap((AggregateEvent) event);
            } else {
                return SerializationAwareEventProxy.wrap(event);
            }
        }
    }
}
