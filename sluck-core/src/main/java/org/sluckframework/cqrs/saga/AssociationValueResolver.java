package org.sluckframework.cqrs.saga;

import org.sluckframework.domain.event.EventProxy;

import java.util.Set;

/**
 * 关联值解析器,用于解析关联值
 *
 * Author: sunxy
 * Created: 2015-09-12 23:13
 * Since: 1.0
 */
public interface AssociationValueResolver {

    /**
     *
     * 从给定的事件提取 关联值
     *
     * @param event The event to extract Association Value from
     * @return The Association Value extracted from the Event, or <code>null</code> if none found.
     */
    Set<AssociationValue> extractAssociationValues(EventProxy<?> event);
}