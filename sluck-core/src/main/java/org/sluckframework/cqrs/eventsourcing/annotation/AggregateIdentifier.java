package org.sluckframework.cqrs.eventsourcing.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表明聚合标识符的注解
 * 
 * @author sunxy
 * @time 2015年9月6日 下午4:16:44	
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggregateIdentifier {

}
