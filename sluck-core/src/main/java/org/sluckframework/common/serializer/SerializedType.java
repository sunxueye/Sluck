package org.sluckframework.common.serializer;
/**
 * 描述了被序列化对象的类型，这个信息为了反序列化而使用
 * 
 * @author sunxy
 * @time 2015年8月29日 下午6:08:35
 * @since 1.0
 */
public interface SerializedType {
	
	/**
	 * 返回被序列列话对象的类型的全称，带包名
     *
     * @return the name of the serialized type
     */
    String getName();

    /**
     * 返回被序列化对象的版本编号，这个版本号在反序列化的时候被upcasters使用
     *
     * @return the revision identifier of the serialized object
     */
    String getRevision();

}
