package org.sluckframework.cqrs.saga.annotation;

import java.lang.annotation.*;

/**
 * 表明saga的开始
 *
 * Author: sunxy
 * Created: 2015-09-13 22:44
 * Since: 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StartSaga {

    /**
     * 是否强制新建saga
     */
    boolean forceNew() default false;
}