package org.sluckframework.cqrs.eventhanding.annotation;

import org.sluckframework.common.annotation.AbstractAnnotatedHandlerDefinition;

/**
 * 注解方法处理定义的实现
 *
 * Author: sunxy
 * Created: 2015-09-21 22:39
 * Since: 1.0
 */
final class AnnotatedEventHandlerDefinition
        extends AbstractAnnotatedHandlerDefinition<EventHandler> {

    public static final AnnotatedEventHandlerDefinition INSTANCE = new AnnotatedEventHandlerDefinition();

    private AnnotatedEventHandlerDefinition() {
        super(EventHandler.class);
    }

    @Override
    protected Class<?> getDefinedPayload(EventHandler annotation) {
        return annotation.eventType();
    }
}