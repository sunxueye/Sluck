package org.sluckframework.common.util;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

/**
 * @author sunxy
 * @time 2015年9月6日 下午4:06:50	
 * @since 1.0
 */
public class CollectionUtils {

    private CollectionUtils() {
        // prevent instantiation
    }

    public static <T> List<T> filterByType(Iterable<?> collection, Class<T> expectedType) {
        List<T> filtered = new LinkedList<T>();
        if (collection != null) {
            for (Object item : collection) {
                if (item != null && expectedType.isInstance(item)) {
                    filtered.add(expectedType.cast(item));
                }
            }
        }
        return filtered;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getAnnotation(Annotation[] annotations, Class<T> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationType)) {
                return (T) annotation;
            }
        }
        return null;
    }

}
