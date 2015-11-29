package org.sluckframework.cqrs.eventhanding.annotation;

import org.sluckframework.common.annotation.ClasspathParameterResolverFactory;
import org.sluckframework.common.annotation.MessageHandlerInvoker;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventhanding.EventListenerProxy;
import org.sluckframework.cqrs.eventhanding.replay.ReplayAware;
import org.sluckframework.domain.event.EventProxy;

/**
 * 将带有处理事件的注解的{@link EventHandler}的方法 转换为 EventListener
 * 
 * Author: sunxy
 * Created: 2015-09-21 22:33
 * Since: 1.0
 */
public class AnnotationEventListenerAdapter implements EventListenerProxy, ReplayAware {

    private final MessageHandlerInvoker invoker;
    private final ReplayAware replayAware;
    private final Class<?> listenerType;

    /**
     * 让指定的 eventbus 订阅指定的 监听器
     *
     * @param annotatedEventListener The annotated event listener
     * @param eventBus               The event bus to subscribe to
     * @return an AnnotationEventListenerAdapter that wraps the listener. Can be used to unsubscribe.
     */
    public static AnnotationEventListenerAdapter subscribe(Object annotatedEventListener, EventBus eventBus) {
        AnnotationEventListenerAdapter adapter = new AnnotationEventListenerAdapter(annotatedEventListener);
        eventBus.subscribe(adapter);
        return adapter;
    }

    /**
     * 让 eventbus 订阅 监听器
     * Wraps the given <code>annotatedEventListener</code>, allowing it to be subscribed to an Event Bus.
     *
     * @param annotatedEventListener the annotated event listener
     */
    public AnnotationEventListenerAdapter(Object annotatedEventListener) {
        this(annotatedEventListener, ClasspathParameterResolverFactory.forClass(annotatedEventListener.getClass()));
    }

    /**
     * 让 eventbus 订阅 监听器
     *
     * @param annotatedEventListener   the annotated event listener
     * @param parameterResolverFactory the strategy for resolving handler method parameter values
     */
    public AnnotationEventListenerAdapter(Object annotatedEventListener,
                                          ParameterResolverFactory parameterResolverFactory) {
        this.invoker = new MessageHandlerInvoker(annotatedEventListener, parameterResolverFactory, false,
                AnnotatedEventHandlerDefinition.INSTANCE);
        this.listenerType = annotatedEventListener.getClass();
        if (annotatedEventListener instanceof ReplayAware) {
            this.replayAware = (ReplayAware) annotatedEventListener;
        } else {
            // as soon as annotations are supported, their handlers should come here...
            this.replayAware = new NoOpReplayAware();
        }
    }

    @Override
    public void handle(EventProxy<?> event) {
        invoker.invokeHandlerMethod(event);
    }

    @Override
    public Class<?> getTargetType() {
        return listenerType;
    }

    @Override
    public void beforeReplay() {
        replayAware.beforeReplay();
    }

    @Override
    public void afterReplay() {
        replayAware.afterReplay();
    }

    @Override
    public void onReplayFailed(Throwable cause) {
        replayAware.onReplayFailed(cause);
    }

    @Override
    public boolean alreadyReplay() {
        return false;
    }

    private static final class NoOpReplayAware implements ReplayAware {

        @Override
        public void beforeReplay() {
        }

        @Override
        public void afterReplay() {
        }

        @Override
        public void onReplayFailed(Throwable cause) {
        }

        @Override
        public boolean alreadyReplay() {
            return false;
        }
    }
}