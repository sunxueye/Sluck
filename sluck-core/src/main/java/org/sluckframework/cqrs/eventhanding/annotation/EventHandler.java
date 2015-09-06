package org.sluckframework.cqrs.eventhanding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表明事件 处理
 * 
 * @author sunxy
 * @time 2015年9月6日 下午4:18:43	
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    Class<?> eventType() default Void.class;
}
