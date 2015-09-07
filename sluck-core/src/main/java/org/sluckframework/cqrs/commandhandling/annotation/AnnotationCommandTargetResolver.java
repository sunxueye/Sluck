package org.sluckframework.cqrs.commandhandling.annotation;

import static java.lang.String.format;
import static org.sluckframework.common.util.ReflectionUtils.ensureAccessible;
import static org.sluckframework.common.util.ReflectionUtils.fieldsOf;
import static org.sluckframework.common.util.ReflectionUtils.getFieldValue;
import static org.sluckframework.common.util.ReflectionUtils.methodsOf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandTargetResolver;
import org.sluckframework.cqrs.commandhandling.VersionedAggregateIdentifier;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 根据注解 提取 标识符 和版本信息
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:48:22	
 * @since 1.0
 */
public class AnnotationCommandTargetResolver implements CommandTargetResolver {

    @Override
    public VersionedAggregateIdentifier resolveTarget(Command<?> command) {
        Identifier<?> aggregateIdentifier;
        Long aggregateVersion;
        try {
            aggregateIdentifier = findIdentifier(command);
            aggregateVersion = findVersion(command);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("An exception occurred while extracting aggregate "
                                                       + "information form a command", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("The current security context does not allow extraction of "
                                                       + "aggregate information from the given command.", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The value provided for the version is not a number.", e);
        }
        if (aggregateIdentifier == null) {
            throw new IllegalArgumentException(
                    format("Invalid command. It does not identify the target aggregate. "
                                   + "Make sure at least one of the fields or methods in the [%s] class contains the "
                                   + "@TargetAggregateIdentifier annotation and that it returns a non-null value.",
                           command.getPayloadType().getSimpleName()));
        }
        return new VersionedAggregateIdentifier(aggregateIdentifier, aggregateVersion);
    }

    @SuppressWarnings("unchecked")
    private <I> I findIdentifier(Command<?> command)
            throws InvocationTargetException, IllegalAccessException {
        for (Method m : methodsOf(command.getPayloadType())) {
            if (m.isAnnotationPresent(TargetAggregateIdentifier.class)) {
                ensureAccessible(m);
                return (I) m.invoke(command.getPayload());
            }
        }
        for (Field f : fieldsOf(command.getPayloadType())) {
            if (f.isAnnotationPresent(TargetAggregateIdentifier.class)) {
                return (I) getFieldValue(f, command.getPayload());
            }
        }
        return null;
    }

    private Long findVersion(Command<?> command) throws InvocationTargetException, IllegalAccessException {
        for (Method m : methodsOf(command.getPayloadType())) {
            if (m.isAnnotationPresent(TargetAggregateVersion.class)) {
                ensureAccessible(m);
                return asLong(m.invoke(command.getPayload()));
            }
        }
        for (Field f : fieldsOf(command.getPayloadType())) {
            if (f.isAnnotationPresent(TargetAggregateVersion.class)) {
                return asLong(getFieldValue(f, command.getPayload()));
            }
        }
        return null;
    }

    private Long asLong(Object fieldValue) {
        if (fieldValue == null) {
            return null;
        } else if (Number.class.isInstance(fieldValue)) {
            return ((Number) fieldValue).longValue();
        } else {
            return Long.parseLong(fieldValue.toString());
        }
    }

}
