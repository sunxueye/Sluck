package org.sluckframework.cqrs.saga;

import org.sluckframework.domain.event.EventProxy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * sagaManager简单实现
 * 
 * Author: sunxy
 * Created: 2015-09-13 21:32
 * Since: 1.0
 */
public class SimpleSagaManager extends AbstractSagaManager {

    private final AssociationValueResolver associationValueResolver;

    private List<Class<?>> eventsToAlwaysCreateNewSagasFor = Collections.emptyList();
    private List<Class<?>> eventsToOptionallyCreateNewSagasFor = Collections.emptyList();
    private final Class<? extends Saga> sagaType;


    /**
     * 使用给定属性初始化
     *
     * @param sagaType                 The type of Saga managed by this SagaManager
     * @param sagaRepository           The repository providing access to Saga instances
     * @param associationValueResolver The instance providing AssociationValues for incoming Events
     */
    public SimpleSagaManager(Class<? extends Saga> sagaType, SagaRepository sagaRepository,
                             AssociationValueResolver associationValueResolver) {
        this(sagaType, sagaRepository, associationValueResolver, new GenericSagaFactory());
    }

    /**
     * 使用给定属性初始化
     *
     * @param sagaType                 The type of Saga managed by this SagaManager
     * @param sagaRepository           The repository providing access to Saga instances
     * @param associationValueResolver The instance providing AssociationValues for incoming Events
     * @param sagaFactory              The factory creating new Saga instances
     */
    @SuppressWarnings("unchecked")
    public SimpleSagaManager(Class<? extends Saga> sagaType, SagaRepository sagaRepository,
                             AssociationValueResolver associationValueResolver, SagaFactory sagaFactory) {
        super(sagaRepository, sagaFactory, sagaType);
        this.sagaType = sagaType;
        this.associationValueResolver = associationValueResolver;
    }

    @Override
    protected SagaInitializationPolicy getSagaCreationPolicy(Class<? extends Saga> type, EventProxy<?> event) {
        AssociationValue initialAssociationValue = initialAssociationValue(event);
        if (isAssignableClassIn(event.getPayloadType(), eventsToOptionallyCreateNewSagasFor)) {
            return new SagaInitializationPolicy(SagaCreationPolicy.IF_NONE_FOUND, initialAssociationValue);
        } else if (isAssignableClassIn(event.getPayloadType(), eventsToAlwaysCreateNewSagasFor)) {
            return new SagaInitializationPolicy(SagaCreationPolicy.ALWAYS, initialAssociationValue);
        } else {
            return SagaInitializationPolicy.NONE;
        }
    }

    @Override
    protected Set<AssociationValue> extractAssociationValues(Class<? extends Saga> type, EventProxy<?> event) {
        return associationValueResolver.extractAssociationValues(event);
    }

    /**
     * Returns the association value to assign to a Saga when the given <code>event</code> triggers the creation of
     * a new instance. If there are no creation handlers for the given <code>event</code>, <code>null</code> is
     * returned.
     *
     * @param event The event to resolve the initial association for
     * @return The association value to assign, or <code>null</code>
     */
    protected AssociationValue initialAssociationValue(EventProxy<?> event) {
        Set<AssociationValue> associations = associationValueResolver.extractAssociationValues(event);
        if (associations.isEmpty()) {
            return null;
        }
        return associations.iterator().next();
    }

    private boolean isAssignableClassIn(Class<?> aClass, Collection<Class<?>> classCollection) {
        for (Class<?> clazz : classCollection) {
            if (aClass != null && clazz.isAssignableFrom(aClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the types of Events that should cause the creation of a new Saga instance, even if one already exists.
     *
     * @param events the types of Events that should cause the creation of a new Saga instance, even if one already
     *               exists
     */
    public void setEventsToAlwaysCreateNewSagasFor(List<Class<?>> events) {
        this.eventsToAlwaysCreateNewSagasFor = events;
    }

    /**
     * Sets the types of Events that should cause the creation of a new Saga instance if one does not already exist.
     *
     * @param events the types of Events that should cause the creation of a new Saga instance if one does not already
     *               exist
     */
    public void setEventsToOptionallyCreateNewSagasFor(List<Class<?>> events) {
        this.eventsToOptionallyCreateNewSagasFor = events;
    }

    @Override
    public Class<?> getTargetType() {
        return sagaType;
    }
    
}
