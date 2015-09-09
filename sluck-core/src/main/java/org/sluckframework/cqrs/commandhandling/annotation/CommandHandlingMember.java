package org.sluckframework.cqrs.commandhandling.annotation;

import java.lang.annotation.*;

/**
 * 表明 命令的处理 方法, 聚合的实体
 *
 * Author: sunxy
 * Created: 2015-09-09 23:28
 * Since: 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface CommandHandlingMember {
}
