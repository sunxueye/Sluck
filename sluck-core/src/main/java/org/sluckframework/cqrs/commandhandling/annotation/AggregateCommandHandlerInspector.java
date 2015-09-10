package org.sluckframework.cqrs.commandhandling.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.common.annotation.AbstractMessageHandler;
import org.sluckframework.common.annotation.MethodMessageHandler;
import org.sluckframework.common.annotation.MethodMessageHandlerInspector;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.exception.SluckConfigurationException;
import org.sluckframework.common.property.Property;
import org.sluckframework.common.property.PropertyAccessStrategy;
import org.sluckframework.common.util.ReflectionUtils;
import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.eventsourcing.annotation.AbstractAnnotatedEntity;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.identifier.Identifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static org.sluckframework.common.util.ReflectionUtils.fieldsOf;

/**
 * 聚合命令方法解析器,用于解析聚合的命令方法注解
 *
 * Author: sunxy
 * Created: 2015-09-09 19:43
 * Since: 1.0
 */
public class AggregateCommandHandlerInspector<T extends AggregateRoot<ID>, ID extends Identifier<?>> {

    private static final Logger logger = LoggerFactory.getLogger(AggregateCommandHandlerInspector.class);

    private final List<ConstructorCommandHandler<T>> constructorCommandHandlers =
            new LinkedList<>();

    private final List<AbstractMessageHandler> handlers;
    /**
     * 使用指定的注解类型初始化,此注解用于描述命令处理方法
     *
     * @param targetType               The targetType to inspect methods on
     * @param parameterResolverFactory The strategy for resolving parameter values
     */
    @SuppressWarnings({"unchecked"})
    protected AggregateCommandHandlerInspector(Class<T> targetType, ParameterResolverFactory parameterResolverFactory) {
        MethodMessageHandlerInspector inspector = MethodMessageHandlerInspector.getInstance(targetType,
                CommandHandler.class,
                parameterResolverFactory,
                true);
        handlers = new ArrayList<>(inspector.getHandlers());
        processNestedEntityCommandHandlers(targetType, parameterResolverFactory, new RootEntityAccessor(targetType));
        for (Constructor constructor : targetType.getConstructors()) {
            if (constructor.isAnnotationPresent(CommandHandler.class)) {
                constructorCommandHandlers.add(
                        ConstructorCommandHandler.forConstructor(constructor, parameterResolverFactory));
            }
        }
    }

    private void processNestedEntityCommandHandlers(Class<?> targetType,
                                                    ParameterResolverFactory parameterResolverFactory,
                                                    final EntityAccessor entityAccessor) {
        for (final Field field : fieldsOf(targetType)) {
            EntityAccessor newEntityAccessor = null;
            if (field.isAnnotationPresent(CommandHandlingMember.class)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Field {}.{} is annotated with @CommandHandlingMember. "
                                    + "Checking {} for Command Handlers",
                            targetType.getSimpleName(), field.getName(), field.getType().getSimpleName()
                    );
                }
                newEntityAccessor = new EntityFieldAccessor(entityAccessor, field);
            } else if (field.isAnnotationPresent(CommandHandlingMemberCollection.class)) {
                CommandHandlingMemberCollection annotation = field.getAnnotation(CommandHandlingMemberCollection.class);
                if (!Collection.class.isAssignableFrom(field.getType())) {
                    throw new SluckConfigurationException(String.format(
                            "Field %s.%s is annotated with @CommandHandlingMemberCollection, but the declared type of "
                                    + "the field is not assignable to java.util.Collection.",
                            targetType.getSimpleName(), field.getName()));
                }
                Class<?> entityType = determineEntityType(annotation.entityType(), field, 0);
                if (entityType == null) {
                    throw new SluckConfigurationException(String.format(
                            "Field %s.%s is annotated with @CommandHandlingMemberCollection, but the entity"
                                    + " type is not indicated on the annotation, "
                                    + "nor can it be deduced from the generic parameters",
                            targetType.getSimpleName(), field.getName()));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Field {}.{} is annotated with @CommandHandlingMemberCollection. "
                                    + "Checking {} for Command Handlers",
                            targetType.getSimpleName(), field.getName(), entityType.getSimpleName()
                    );
                }
                newEntityAccessor = new EntityCollectionFieldAccessor(entityType, annotation, entityAccessor, field);
            } else if (field.isAnnotationPresent(CommandHandlingMemberMap.class)) {
                CommandHandlingMemberMap annotation = field.getAnnotation(CommandHandlingMemberMap.class);
                if (!Map.class.isAssignableFrom(field.getType())) {
                    throw new SluckConfigurationException(String.format(
                            "Field %s.%s is annotated with @CommandHandlingMemberMap, but the declared type of "
                                    + "the field is not assignable to java.util.Map.",
                            targetType.getSimpleName(), field.getName()));
                }
                Class<?> entityType = determineEntityType(annotation.entityType(), field, 1);
                if (entityType == null) {
                    throw new SluckConfigurationException(String.format(
                            "Field %s.%s is annotated with @CommandHandlingMemberMap, but the entity"
                                    + " type is not indicated on the annotation, "
                                    + "nor can it be deduced from the generic parameters",
                            targetType.getSimpleName(), field.getName()));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Field {}.{} is annotated with @CommandHandlingMemberMap. "
                                    + "Checking {} for Command Handlers",
                            targetType.getSimpleName(), field.getName(), entityType.getSimpleName()
                    );
                }
                newEntityAccessor = new EntityMapFieldAccessor(entityType, annotation, entityAccessor, field);
            }
            if (newEntityAccessor != null) {
                MethodMessageHandlerInspector fieldInspector = MethodMessageHandlerInspector
                        .getInstance(newEntityAccessor.entityType(),
                                CommandHandler.class,
                                parameterResolverFactory,
                                true);
                for (MethodMessageHandler fieldHandler : fieldInspector.getHandlers()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found a Command Handler in {} on field {}.{}",
                                field.getType().getSimpleName(),
                                entityAccessor.entityType().getName(),
                                field.getName());
                    }
                    handlers.add(new EntityForwardingMethodMessageHandler(newEntityAccessor, fieldHandler));
                }
                processNestedEntityCommandHandlers(field.getType(), parameterResolverFactory,
                        newEntityAccessor);
            }
        }
    }

    private Class<?> determineEntityType(Class<?> entityType, Field field, int genericTypeIndex) {
        if (AbstractAnnotatedEntity.class.equals(entityType)) {
            final Type genericType = field.getGenericType();
            if (genericType == null
                    || !(genericType instanceof ParameterizedType)
                    || ((ParameterizedType) genericType).getActualTypeArguments().length == 0) {
                return null;
            }
            entityType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[genericTypeIndex];
        }
        return entityType;
    }

    public List<ConstructorCommandHandler<T>> getConstructorHandlers() {
        return constructorCommandHandlers;
    }

    public List<AbstractMessageHandler> getHandlers() {
        return handlers;
    }

    private interface EntityAccessor {

        Object getInstance(Object aggregateRoot, Command<?> commandMessage) throws IllegalAccessException;

        Class<?> entityType();
    }

    private static class EntityForwardingMethodMessageHandler extends AbstractMessageHandler {

        private final AbstractMessageHandler handler;
        private final EntityAccessor entityAccessor;

        public EntityForwardingMethodMessageHandler(EntityAccessor entityAccessor, AbstractMessageHandler handler) {
            super(handler);
            this.entityAccessor = entityAccessor;
            this.handler = handler;
        }

        @Override
        public Object invoke(Object target, EventProxy<?> message) throws InvocationTargetException, IllegalAccessException {
            Object entity = entityAccessor.getInstance(target, (Command<?>) message);
            if (entity == null) {
                throw new IllegalStateException("No appropriate entity available in the aggregate. "
                        + "The command cannot be handled.");
            }
            return handler.invoke(entity, message);
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return handler.getAnnotation(annotationType);
        }
    }

    private static class EntityFieldAccessor implements EntityAccessor {

        private final EntityAccessor entityAccessor;
        private final Field field;

        public EntityFieldAccessor(EntityAccessor parent, Field field) {
            this.entityAccessor = parent;
            this.field = field;
        }

        @Override
        public Class<?> entityType() {
            return field.getType();
        }

        @Override
        public Object getInstance(Object aggregateRoot, Command<?> commandMessage)
                throws IllegalAccessException {
            Object entity = entityAccessor.getInstance(aggregateRoot, commandMessage);
            return entity != null ? ReflectionUtils.getFieldValue(field, entity) : null;
        }
    }

    private static class RootEntityAccessor implements EntityAccessor {

        private final Class<?> entityType;

        private RootEntityAccessor(Class<?> entityType) {
            this.entityType = entityType;
        }

        @Override
        public Object getInstance(Object aggregateRoot, Command<?> commandMessage) {
            return aggregateRoot;
        }

        @Override
        public Class<?> entityType() {
            return entityType;
        }
    }

    private abstract class MultipleEntityFieldAccessor<R> implements EntityAccessor {

        private final Class<?> entityType;
        private final EntityAccessor entityAccessor;
        private final Field field;
        private String commandTargetProperty;


        @SuppressWarnings("unchecked")
        public MultipleEntityFieldAccessor(Class entityType, String commandTargetProperty,
                                           EntityAccessor entityAccessor, Field field) {
            this.entityType = entityType;
            this.entityAccessor = entityAccessor;
            this.commandTargetProperty = commandTargetProperty;
            this.field = field;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object getInstance(Object aggregateRoot, Command command) throws IllegalAccessException {
            final Object parentEntity = entityAccessor.getInstance(aggregateRoot, command);
            if (parentEntity == null) {
                return null;
            }
            R entityCollection = (R) ReflectionUtils.getFieldValue(field, parentEntity);
            Property<Object> commandProperty = PropertyAccessStrategy.getProperty(command.getPayloadType(), commandTargetProperty);

            if (commandProperty == null) {
                // TODO: Log failure. It seems weird that the property is not present
                return null;
            }
            Object commandId = commandProperty.getValue(command.getPayload());
            if (commandId == null) {
                return null;
            }
            return getEntity(entityCollection, commandId);
        }

        protected abstract Object getEntity(R entities, Object commandId);

        @Override
        public Class<?> entityType() {
            return entityType;
        }
    }

    private class EntityCollectionFieldAccessor extends MultipleEntityFieldAccessor<Collection<?>> {
        private final Property<Object> entityProperty;

        @SuppressWarnings("unchecked")
        public EntityCollectionFieldAccessor(Class entityType, CommandHandlingMemberCollection annotation,
                                             EntityAccessor entityAccessor, Field field) {
            super(entityType, annotation.commandTargetProperty(), entityAccessor, field);
            this.entityProperty = PropertyAccessStrategy.getProperty(entityType, annotation.entityId());
        }

        protected Object getEntity(Collection<?> entities, Object commandId) {
            for (Object entity : entities) {
                Object entityId = entityProperty.getValue(entity);
                if (entityId != null && entityId.equals(commandId)) {
                    return entity;
                }
            }
            return null;
        }

    }

    private class EntityMapFieldAccessor extends MultipleEntityFieldAccessor<Map<?, ?>> {

        public EntityMapFieldAccessor(Class entityType, CommandHandlingMemberMap annotation,
                                      EntityAccessor entityAccessor, Field field) {
            super(entityType, annotation.commandTargetProperty(), entityAccessor, field);
        }

        @Override
        protected Object getEntity(Map<?, ?> entities, Object commandId) {
            return entities.get(commandId);
        }
    }
}


