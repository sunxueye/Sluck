package org.sluckframework.cqrs.commandhandling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表明字段为 聚合的 标识符
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:52:38	
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TargetAggregateIdentifier {
}