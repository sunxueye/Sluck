package org.sluckframework.cqrs.commandhandling.annotation;

import org.sluckframework.cqrs.eventsourcing.annotation.AbstractAnnotatedEntity;

import java.lang.annotation.*;

/**
 * 标示注解的字段有处理事件的能力
 *
 * Author: sunxy
 * Created: 2015-09-10 10:49
 * Since: 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface CommandHandlingMemberMap {

    /**
     * command's payload的全限定名称,这个标示应该为 map 中的key
     */
    String commandTargetProperty();

    /**
     * 集合中包含的实体类型
     *
     */
    Class<? extends AbstractAnnotatedEntity> entityType() default AbstractAnnotatedEntity.class;
}
