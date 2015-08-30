package org.sluckframework.cqrs.upcasting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sluckframework.common.serializer.ConverterFactory;
import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.SerializedType;
import org.sluckframework.common.serializer.Serializer;

/**
 * upcasterChain的简单实现
 * 
 * @author sunxy
 * @time 2015年8月30日 上午1:41:09
 * @since 1.0
 */
public class SimpleUpcasterChain extends AbstractUpcasterChain {
	
	/**
     * empty UpcasterChain.
     */
    public static final UpcasterChain EMPTY = new SimpleUpcasterChain(Collections.<Upcaster<?>>emptyList());

    /**
     * 使用给定 upcasters 和默认的 ChainingConverterFactory进行初始化
     *
     * @param upcasters the upcasters to form the chain
     */
    public SimpleUpcasterChain(List<Upcaster<?>> upcasters) {
        super(upcasters);
    }
    
    /**
     * 使用给定 upcasters 和 serializer的 ChainingConverterFactory进行初始化
     *
     * @param upcasters the upcasters to form the chain
     */

    public SimpleUpcasterChain(Serializer serializer, List<Upcaster<?>> upcasters) {
        super(serializer.getConverterFactory(), upcasters);
    }

    /**
     * 使用给定 upcasters 和ChainingConverterFactory进行初始化
     *
     * @param converterFactory The factory providing the converters to convert between content types
     * @param upcasters        The upcasters to form this chain
     */
    public SimpleUpcasterChain(ConverterFactory converterFactory, List<Upcaster<?>> upcasters) {
        super(converterFactory, upcasters);
    }

    /**
     * 使用给定 upcasters数组 和ChainingConverterFactory进行初始化
     *
     * @param converterFactory The factory providing ContentTypeConverter instances
     * @param upcasters The upcasters forming the chain (in given order)
     */
    public SimpleUpcasterChain(ConverterFactory converterFactory, Upcaster<?>... upcasters) {
        this(converterFactory, Arrays.asList(upcasters));
    }

    @Override
    protected <T> List<SerializedObject<?>> doUpcast(Upcaster<T> upcaster, SerializedObject<?> sourceObject,
                                                     List<SerializedType> targetTypes,
                                                     UpcastingContext context) {
        SerializedObject<T> converted = ensureCorrectContentType(sourceObject,
                                                                 upcaster.expectedRepresentationType());
        return upcaster.upcast(converted, targetTypes, context);
    }

}
