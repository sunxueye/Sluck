package org.sluckframework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解的 组件 优先权判断
 * 
 * @author sunxy
 * @time 2015年9月6日 下午4:28:39	
 * @since 1.0
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {

    /**
     * Value indicating the annotated member should come last.
     */
    int LAST = Integer.MIN_VALUE;
    /**
     * Value indicating the annotated member should be placed at the "lower half".
     */
    int LOW = Integer.MIN_VALUE / 2;
    /**
     * Value indicating the annotated member should have medium priority, effectively placing it "in the middle".
     */
    int NEUTRAL = 0;
    /**
     * Value indicating the annotated member should have high priority, effectively placing it "in the first half".
     */
    int HIGH = Integer.MAX_VALUE / 2;
    /**
     * Value indicating the annotated member should be the very first
     */
    int FIRST = Integer.MAX_VALUE;


    /**
     * A value indicating the priority. Members with higher values must come before members with a lower value
     */
    int value() default NEUTRAL;
}
