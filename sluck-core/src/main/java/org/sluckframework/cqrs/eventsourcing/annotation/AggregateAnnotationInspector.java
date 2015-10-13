package org.sluckframework.cqrs.eventsourcing.annotation;

import org.sluckframework.common.annotation.HandlerDefinition;
import org.sluckframework.common.annotation.MessageHandlerInvoker;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.util.ReflectionUtils;
import org.sluckframework.cqrs.eventhanding.annotation.EventHandler;
import org.sluckframework.cqrs.eventsourcing.EventSourcedEntity;
import org.sluckframework.cqrs.eventsourcing.IncompatibleAggregateException;
import org.sluckframework.domain.identifier.Identifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.sluckframework.common.util.CollectionUtils.filterByType;
import static org.sluckframework.common.util.ReflectionUtils.ensureAccessible;
import static org.sluckframework.common.util.ReflectionUtils.fieldsOf;
/**
 * 聚合的 注解 解析
 * 
 * @author sunxy
 * @time 2015年9月6日 下午3:59:51	
 * @since 1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AggregateAnnotationInspector {

    private static final Map<Class<?>, AggregateAnnotationInspector> INSTANCES = new ConcurrentHashMap<Class<?>, AggregateAnnotationInspector>();
    private final Field[] childEntityFields;
    private final Field identifierField;
    private final ParameterResolverFactory parameterResolverFactory;

    /**
     * 使用给定的 实体类型 和  参数解析工厂 返回 聚合注解解析器
     * 
     * @param entityType               The type of entity (aggregate root or simple member) to get an inspector for
     * @param parameterResolverFactory The factory providing access to the parameter resolvers
     * @return an inspector for the given entity type
     */
    public static AggregateAnnotationInspector getInspector(Class<?> entityType,
                                                            ParameterResolverFactory parameterResolverFactory) {
        AggregateAnnotationInspector inspector = INSTANCES.get(entityType);
        if (inspector == null || !parameterResolverFactory.equals(inspector.parameterResolverFactory)) {
            inspector = new AggregateAnnotationInspector(entityType, parameterResolverFactory);
            INSTANCES.put(entityType, inspector);
        }
        return inspector;
    }

    private AggregateAnnotationInspector(Class<?> entityType, ParameterResolverFactory parameterResolverFactory) {
        List<Field> annotatedFields = new ArrayList<>();
        for (Field field : ReflectionUtils.fieldsOf(entityType)) {
            if (field.isAnnotationPresent(EventSourcedMember.class)) {
                annotatedFields.add(field);
            }
        }
        childEntityFields = annotatedFields.toArray(new Field[annotatedFields.size()]);
        // if entityType is an aggregate root, detect it's identifier field
        if (AbstractAnnotatedAggregateRoot.class.isAssignableFrom(entityType)) {
            identifierField = locateIdentifierField((Class<? extends AbstractAnnotatedAggregateRoot>) entityType);
        } else {
            identifierField = null;
        }
        this.parameterResolverFactory = parameterResolverFactory;
    }

    /**
     * 用给定的目标对象创建一个新的 事件处理器
     * Creates a new MessageHandlerInvoker that invokes methods on the given <code>instance</code>.
     *
     * @param instance The object (typically an entity) to create the MessageHandlerInvoker for
     * @return a MessageHandlerInvoker that invokes handler methods on given <code>instance</code>
     */
    public MessageHandlerInvoker createEventHandlerInvoker(Object instance) {
        return new MessageHandlerInvoker(instance, parameterResolverFactory, false,
                                         AggregatedEventSourcingHandlerDefinition.INSTANCE);
    }

    /**
     * 获取 实例的 child 实体.支持 map or array 的实体注解， 集合中 需要是 EventSourcedEntity
     *
     * @param instance The instance to find child entities in
     * @return a collection of child entities found in the given <code>instance</code>.
     */
    public Collection<EventSourcedEntity> getChildEntities(Object instance) {
        if (childEntityFields.length == 0 || instance == null) {
            return null;
        }
        List<EventSourcedEntity> children = new ArrayList<EventSourcedEntity>();
        for (Field childEntityField : childEntityFields) {
            Object fieldValue = ReflectionUtils.getFieldValue(childEntityField, instance);
            if (EventSourcedEntity.class.isInstance(fieldValue)) {
                children.add((EventSourcedEntity) fieldValue);
            } else if (Iterable.class.isInstance(fieldValue)) {
                // it's a collection
                Iterable<?> iterable = (Iterable<?>) fieldValue;
                children.addAll(filterByType(iterable, EventSourcedEntity.class));
            } else if (Map.class.isInstance(fieldValue)) {
                Map map = (Map) fieldValue;
                children.addAll(filterByType(map.keySet(), EventSourcedEntity.class));
                children.addAll(filterByType(map.values(), EventSourcedEntity.class));
            } else if (fieldValue != null && childEntityField.getType().isArray()) {
                for (int i = 0; i < Array.getLength(fieldValue); i++) {
                    Object value = Array.get(fieldValue, i);
                    if (EventSourcedEntity.class.isInstance(value)) {
                        children.add((EventSourcedEntity) value);
                    }
                }
            }
        }
        return children;
    }

    /**
     * 根据给定的 注解 的 聚合根 获取其 标识符
     *
     * @param aggregateRoot The aggregate root to find the aggregate on
     * @param <I>           The type of identifier declared on the aggregate root
     * @return the value contained in the field annotated with {@link AggregateIdentifier}
     */
    public <I extends Identifier<?>> I getIdentifier(AbstractAnnotatedAggregateRoot<I> aggregateRoot) {
        if (identifierField == null) {
            throw new IncompatibleAggregateException(
                    format("The aggregate class [%s] does not specify an Identifier. "
                                   + "Ensure that the field containing the aggregate "
                                   + "identifier is annotated with @AggregateIdentifier.",
                           aggregateRoot.getClass().getSimpleName()));
        }
        return (I) ReflectionUtils.getFieldValue(identifierField, aggregateRoot);
    }

    private Field locateIdentifierField(Class<? extends AbstractAnnotatedAggregateRoot> aggregateRootType) {
        for (Field candidate : fieldsOf(aggregateRootType)) {
            if (containsIdentifierAnnotation(candidate.getAnnotations())) {
                ensureAccessible(candidate);
                return candidate;
            }
        }
        return null;
    }

    private boolean containsIdentifierAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof AggregateIdentifier) {
                return true;
            } else if (annotation.toString().startsWith("@javax.persistence.Id(")) {
                // this way, the JPA annotations don't need to be on the classpath
                return true;
            }
        }
        return false;
    }

    private static class AggregatedEventSourcingHandlerDefinition implements HandlerDefinition<Method> {

        private static final AggregatedEventSourcingHandlerDefinition INSTANCE = new AggregatedEventSourcingHandlerDefinition();

        @Override
        public boolean isMessageHandler(Method member) {
            return member.isAnnotationPresent(EventSourcingHandler.class)
                    || member.isAnnotationPresent(EventHandler.class);
        }

        @Override
        public Class<?> resolvePayloadFor(Method member) {
            EventSourcingHandler handlerAnnotation = member.getAnnotation(EventSourcingHandler.class);
            Class<?> definedPayload = null;
            if (handlerAnnotation != null) {
                definedPayload = handlerAnnotation.eventType();
            } else {
                EventHandler legacyAnnotation = member.getAnnotation(EventHandler.class);
                if (legacyAnnotation != null) {
                    definedPayload = legacyAnnotation.eventType();
                }
            }
            return definedPayload == Void.class ? null : definedPayload;
        }

        @Override
        public String toString() {
            return "AnnotatedEventSourcingMemberDefinition";
        }
    }

}
