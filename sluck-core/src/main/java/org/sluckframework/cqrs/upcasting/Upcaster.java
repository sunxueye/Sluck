package org.sluckframework.cqrs.upcasting;

import java.util.List;

import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.SerializedType;

/**
 * 用于对象的转换，可用于将旧的序列化对象的版本转换为最新的版本，常用于重构事件的时候，历史事件与现事件的兼容问题
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:16:15
 * @since 1.0
 */
public interface Upcaster<T> {

    /**
     * 判断给定的type 能否被转换
     * 
     * @param serializedType The type under investigation
     * @return true if this upcaster can upcast the given serialized type, false otherwise.
     */
    boolean canUpcast(SerializedType serializedType);

    /**
     * 目标转换类型
     * 
     * @return the type of intermediate representation expected
     */
    Class<T> expectedRepresentationType();

    /**
     * 将旧的SerializedObject 转换为 一个或多个 新的SerializedObject, 返回的 SerializedObject 类型必须匹配 给定的 type list
     *
     * @param intermediateRepresentation The representation of the object to upcast
     * @param expectedTypes              The expected types of the returned serialized objects.
     * @param context                    An instance describing the context of the object to upcast
     * @return the new representations of the object
     */
    List<SerializedObject<?>> upcast(SerializedObject<T> intermediateRepresentation,
                                     List<SerializedType> expectedTypes, UpcastingContext context);

    /**
     * 转换给定的 serializedType 到最新的 一个或多个 serializedType，如果是多个，那么顺序需要和 返回的多个SerializedObject顺序对应
     *
     * @param serializedType The serialized type to upcast
     * @return the upcast serialized type
     */
    List<SerializedType> upcast(SerializedType serializedType);

}
