package org.sluckframework.common.annotation;

import java.util.Comparator;


/**
 * 基于 注解 @Priority 比较 
 * 
 * @author sunxy
 * @time 2015年9月6日 下午4:27:02	
 * @since 1.0
 */
public class PriorityAnnotationComparator<T> implements Comparator<T> {

    @SuppressWarnings("rawtypes")
	private static final PriorityAnnotationComparator INSTANCE = new PriorityAnnotationComparator();

    /**
     * Returns the instance of the comparator.
     *
     * @param <T> The type of values to compare
     * @return a comparator comparing objects based on their @Priority annotations
     */
    @SuppressWarnings("unchecked")
    public static <T> PriorityAnnotationComparator<T> getInstance() {
        return INSTANCE;
    }

    private PriorityAnnotationComparator() {
    }

    @Override
    public int compare(T o1, T o2) {
        Priority annotation1 = o1.getClass().getAnnotation(Priority.class);
        Priority annotation2 = o2.getClass().getAnnotation(Priority.class);
        int prio1 = annotation1 == null ? Priority.NEUTRAL : annotation1.value();
        int prio2 = annotation2 == null ? Priority.NEUTRAL : annotation2.value();

        return (prio1 > prio2) ? -1 : ((prio2 == prio1) ? 0 : 1);
    }

}
