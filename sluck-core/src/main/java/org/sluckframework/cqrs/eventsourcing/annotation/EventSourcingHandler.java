package org.sluckframework.cqrs.eventsourcing.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级别的 注解， 表示 处理 聚合事件
 * 
 * @author sunxy
 * @time 2015年9月6日 下午4:15:14	
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventSourcingHandler {

    /**
     * 指定 方法处理的类型，如果不指定 处理所有
     */
    Class<?> eventType() default Void.class;
}
