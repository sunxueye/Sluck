package org.sluckframework.cqrs.saga.annotation;

import java.lang.annotation.*;

/**
 * 表明saga的结束
 *
 * Author: sunxy
 * Created: 2015-09-13 22:45
 * Since: 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EndSaga {

}
