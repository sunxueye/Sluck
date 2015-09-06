package org.sluckframework.cqrs.eventsourcing.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;

import org.sluckframework.common.annotation.HandlerDefinition;


/**
 * 注解的 处理器 定义
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:31:54	
 * @since 1.0
 */
public abstract class AbstractAnnotatedHandlerDefinition<T extends Annotation>
		implements HandlerDefinition<AccessibleObject> {

    private final Class<T> annotationType;

    /**
     * 使用给定的 注解类型 初始化
     *
     * @param annotationType The type of annotation that marks the handlers
     */
    protected AbstractAnnotatedHandlerDefinition(Class<T> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public boolean isMessageHandler(AccessibleObject member) {
        return member.isAnnotationPresent(annotationType);
    }

    @Override
    public Class<?> resolvePayloadFor(AccessibleObject member) {
        T annotation = member.getAnnotation(annotationType);
        Class<?> definedPayload = null;
        if (annotation != null) {
            definedPayload = getDefinedPayload(annotation);
            if (definedPayload == Void.class) {
                return null;
            }
        }
        return definedPayload;
    }

    /**
     * 根据注解 获取 定义的 payLoad类型
     *
     * @param annotation The annotation that defines this method to be a handler
     * @return the explicit payload type for this handler
     */
    protected abstract Class<?> getDefinedPayload(T annotation);

    @Override
    public String toString() {
        return "AnnotatedHandler{" + annotationType + '}';
    }

}
