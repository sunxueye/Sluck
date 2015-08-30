package org.sluckframework.common.serializer;


/**
 * 描述了 被序列化对象的信息
 * 
 * @author sunxy
 * @time 2015年8月29日 下午6:05:56
 * @since 1.0
 */
public interface SerializedObject<T> {
	
	
    /**
     * 返回被序列化后对象的类型
     *
     * @return after type
     */
    Class<T> getContentType();

    /**
     * 返回被序列化之前的类型
     *
     * @return before type
     */
    SerializedType getType();

    /**
     * 持有的序列后的对象.
     *
     * @return the actual data of the serialized object
     */
    T getData();

}
