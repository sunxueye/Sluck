package org.sluckframework.cqrs.eventsourcing.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段级别的注解，表明 指定的 字段 为聚合的 chilid
 * 
 * @author sunxy
 * @time 2015年9月6日 下午4:03:59	
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EventSourcedMember {

}
