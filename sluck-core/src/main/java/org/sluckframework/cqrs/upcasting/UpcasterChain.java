package org.sluckframework.cqrs.upcasting;

import java.util.List;

import org.sluckframework.common.serializer.SerializedObject;


/**
 * 转换链，用于组合使用，转换旧的 SerializedObject 到最新的 Payload版本，可能需要使用 converterFactory提供的 converters来转换
 * 
 * @author sunxy
 * @time 2015年8月29日 下午5:50:49
 * @since 1.0
 */
public interface UpcasterChain {
	
	/**
	 * 传递一个 旧的serializedObject，结果返回一个或多个 新的serializedObjects，包含了最新的payload版本
	 *
	 * @param serializedObject the serialized object to upcast
	 * @param upcastingContext
	 * @return the upcast SerializedObjects
	 */
	@SuppressWarnings("rawtypes")
	List<SerializedObject> upcast(SerializedObject serializedObject, UpcastingContext upcastingContext);
}
