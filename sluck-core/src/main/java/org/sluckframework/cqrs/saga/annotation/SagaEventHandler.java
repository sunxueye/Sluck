package org.sluckframework.cqrs.saga.annotation;

import java.lang.annotation.*;

/**
 * saga的事件处理器,第一个参数为需要处理的事件,对于每个事件来说,此方法只会执行一次,所有对有继承体系来说,说有顺序的
 *
 *
 * Author: sunxy
 * Created: 2015-09-13 23:16
 * Since: 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SagaEventHandler {

    /**
     * 帮助去找到 saga 实例,通常为 saga 的标示符
     * The property in the event that will provide the value to find the Saga instance. Typically, this value is an
     * aggregate identifier of an aggregate that a specific saga monitors.
     */
    String associationProperty();

    /**
     * 关联值的 key
     */
    String keyName() default "";

    /**
     * 方法处理的事件的类型,没有定义则使用第一个参数的类型
     */
    Class<?> payloadType() default Void.class;
}