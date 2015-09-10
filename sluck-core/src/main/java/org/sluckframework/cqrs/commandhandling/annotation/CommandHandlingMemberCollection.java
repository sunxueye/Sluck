package org.sluckframework.cqrs.commandhandling.annotation;

import org.sluckframework.cqrs.eventsourcing.annotation.AbstractAnnotatedEntity;

import java.lang.annotation.*;

/**
 * 表示字段有处理事件的能力
 *
 * Author: sunxy
 * Created: 2015-09-10 10:43
 * Since: 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface CommandHandlingMemberCollection {

    /**
     * 表明实体标示符的名称
     */
    String entityId();

    /**
     * command 中的 payload的类型的全限定名称
     */
    String commandTargetProperty();

    /**
     * 在注解集合中包含的实体的类型
     */
    Class<? extends AbstractAnnotatedEntity> entityType() default AbstractAnnotatedEntity.class;
}
