package org.sluckframework.cqrs.eventsourcing.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 聚合根标示,用于标示类为聚合根
 *
 * Author: sunxy
 * Created: 2015-10-22 00:00
 * Since: 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggregateRoot {
}
