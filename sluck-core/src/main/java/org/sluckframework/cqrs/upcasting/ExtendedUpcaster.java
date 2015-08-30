package org.sluckframework.cqrs.upcasting;

import java.util.List;

import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.SerializedType;


/**
 * upcaster扩展接口
 * 
 * @author sunxy
 * @time 2015年8月30日 上午1:31:48
 * @since 1.0
 */
public interface ExtendedUpcaster<T> extends Upcaster<T> {
	
	/**
	 * Upcast 给定的 serializedType 到最新的版本，方法的执行会增加版本，也就是 新的 serializedType name 和旧的一致，升级版本
	 * 实现此接口的 upcaster 必须使用此方法代替 {@link #upcast(org.sluckframework.common.serializer.SerializedType)}
     *
     * @param serializedType             The serialized type to upcast
     * @param intermediateRepresentation The intermediate representation of the object to define the type for
     * @return the upcast serialized type
     */
    List<SerializedType> upcast(SerializedType serializedType, SerializedObject<T> intermediateRepresentation);

    @Override
    List<SerializedType> upcast(SerializedType serializedType);

}
