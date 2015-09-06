package org.sluckframework.cqrs.eventsourcing;

import static java.lang.String.format;
import static org.sluckframework.common.util.ReflectionUtils.ensureAccessible;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.identifier.Identifier;



/**
 * 通用聚合工厂
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:56:56	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class GenericAggregateFactory<T extends EventSourcedAggregateRoot> extends AbstractAggregateFactory<T> {

    private final String typeIdentifier;
    private final Class<T> aggregateType;
    private final Constructor<T> constructor;

    /**
     * 使用给定的 聚合 类型初始化
     *
     * @param aggregateType The type of aggregate this factory creates instances of.
     */
    public GenericAggregateFactory(Class<T> aggregateType) {
        this(aggregateType, null);
    }

    /**
     * 使用给定的聚合类型 和参数解析工厂 初始化 参数解析工厂用于解析 注解参数，这个构造函数 只用于
     * 聚合是 {@code
     * org.sluckframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot} 的类型
     * @param aggregateType            The type of aggregate this factory creates instances of.
     * @param parameterResolverFactory THe factory that resolves parameters of annotated event handlers
     */
    public GenericAggregateFactory(Class<T> aggregateType, ParameterResolverFactory parameterResolverFactory) {
        super(parameterResolverFactory);
        Assert.isTrue(EventSourcedAggregateRoot.class.isAssignableFrom(aggregateType),
                      "The given aggregateType must be a subtype of EventSourcedAggregateRoot");
        Assert.isFalse(Modifier.isAbstract(aggregateType.getModifiers()), "Given aggregateType may not be abstract");
        this.aggregateType = aggregateType;
        this.typeIdentifier = aggregateType.getSimpleName();
        try {
            this.constructor = ensureAccessible(aggregateType.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            throw new IncompatibleAggregateException(format("The aggregate [%s] doesn't provide a no-arg constructor.",
                                                            aggregateType.getSimpleName()), e);
        }
    }

	@Override
    protected T doCreateAggregate(Identifier<?> aggregateIdentifier, AggregateEvent firstEvent) {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new IncompatibleAggregateException(format(
                    "The aggregate [%s] does not have a suitable no-arg constructor.",
                    aggregateType.getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new IncompatibleAggregateException(format(
                    "The aggregate no-arg constructor of the aggregate [%s] is not accessible. Please ensure that "
                            + "the constructor is public or that the Security Manager allows access through "
                            + "reflection.", aggregateType.getSimpleName()), e);
        } catch (InvocationTargetException e) {
            throw new IncompatibleAggregateException(format(
                    "The no-arg constructor of [%s] threw an exception on invocation.",
                    aggregateType.getSimpleName()), e);
        }
    }

    @Override
    public String getTypeIdentifier() {
        return typeIdentifier;
    }

    @Override
    public Class<T> getAggregateType() {
        return aggregateType;
    }

}
