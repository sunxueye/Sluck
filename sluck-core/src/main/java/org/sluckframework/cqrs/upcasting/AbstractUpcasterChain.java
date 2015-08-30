package org.sluckframework.cqrs.upcasting;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.serializer.ChainingConverterFactory;
import org.sluckframework.common.serializer.ContentTypeConverter;
import org.sluckframework.common.serializer.ConverterFactory;
import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.SerializedType;

/**
 * upcasterChain的抽象实现，用于迭代 convert content type
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:14:52
 * @since 1.0
 */
public abstract class AbstractUpcasterChain implements UpcasterChain{
	

    private final List<Upcaster<?>> upcasters;
    private final ConverterFactory converterFactory;

    /**
     * 使用给定的 upcasters 和 默认的 ChainingConverterFactory 初始化
     *
     * @param upcasters the upcasters to form the chain
     */
    protected AbstractUpcasterChain(List<Upcaster<?>> upcasters) {
        this(new ChainingConverterFactory(), upcasters);
    }

    /**
     * 根据给定的 converFactory 和 upcasters　list 初始化
     *
     * @param converterFactory The factory providing the converters to convert between content types
     * @param upcasters        The upcasters to form this chain
     */
    protected AbstractUpcasterChain(ConverterFactory converterFactory, List<Upcaster<?>> upcasters) {
        Assert.notNull(converterFactory, "converterFactory may not be null");
        this.upcasters = upcasters;
        this.converterFactory = converterFactory;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public List<SerializedObject> upcast(SerializedObject serializedObject, UpcastingContext upcastingContext) {
        if (upcasters.isEmpty()) {
            return singletonList(serializedObject);
        }
        Iterator<Upcaster<?>> upcasterIterator = upcasters.iterator();
        return upcastInternal(singletonList(serializedObject), upcasterIterator, upcastingContext);
    }

    /**
     * 将旧的 serializedObject converter为 新的 serializedObject,如果expectedContentType 和旧的一致，那么直接返回
     * 只是一个确认操作
     *
     * @param serializedObject    The object to convert
     * @param expectedContentType The content type of the SerializedObject to return
     * @return a SerializedObject containing data in the expected content type
     */
    @SuppressWarnings("unchecked")
	protected <S, T> SerializedObject<T> ensureCorrectContentType(SerializedObject<S> serializedObject,
                                                                  Class<T> expectedContentType) {
        if (!expectedContentType.isAssignableFrom(serializedObject.getContentType())) {
            ContentTypeConverter<S, T> converter = converterFactory.getConverter(serializedObject.getContentType(),
                                                                                 expectedContentType);
            return converter.convert(serializedObject);
        }
        return (SerializedObject<T>) serializedObject;
    }

    /**
     * 使用给定的 upcaster 去 upcaster 旧的 SerializedObject 到新的 SerializedObject list,类型需要和 targetTypes匹配
     * 
     * @param upcaster     The upcaster to perform the upcasting with
     * @param sourceObject The SerializedObject to upcast
     * @param targetTypes  The types expected in the returned List of SerializedObject
     * @param context      The container of properties of the Domain Event Message being upcast
     * @return The List of SerializedObject representing the upcast <code>sourceObject</code>
     */
    protected abstract <T> List<SerializedObject<?>> doUpcast(Upcaster<T> upcaster, SerializedObject<?> sourceObject,
                                                              List<SerializedType> targetTypes,
                                                              UpcastingContext context);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<SerializedObject> upcastInternal(List<SerializedObject> serializedObjects,
                                                  Iterator<Upcaster<?>> upcasterIterator,
                                                  UpcastingContext context) {
        if (!upcasterIterator.hasNext()) {
            return serializedObjects;
        }
        List<SerializedObject> upcastObjects = new ArrayList<SerializedObject>();
        Upcaster<?> currentUpcaster = upcasterIterator.next();
        for (SerializedObject serializedObject : serializedObjects) {
            if (currentUpcaster.canUpcast(serializedObject.getType())) {
                List<SerializedType> upcastTypes;
                if (currentUpcaster instanceof ExtendedUpcaster) {
                    upcastTypes = ((ExtendedUpcaster) currentUpcaster).upcast(serializedObject.getType(),
                                                                              serializedObject);
                } else {
                    upcastTypes = currentUpcaster.upcast(serializedObject.getType());
                }
                upcastObjects.addAll(doUpcast(currentUpcaster, serializedObject, upcastTypes, context));
            } else {
                upcastObjects.add(serializedObject);
            }
        }
        return upcastInternal(upcastObjects, upcasterIterator, context);
    }

}
