package org.sluckframework.cqrs.commandhandling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解的方法表示为命令处理方法
 *
 * Author: sunxy
 * Created: 2015-09-09 19:56
 * Since: 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface CommandHandler {

    /**
     * 处理命令的名称,默认为方法的第一个参数的payload的全限定名称
     */
    String commandName() default "";
}
