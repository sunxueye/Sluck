package org.sluckframework.common.serializer;
/**
 * 根据指定的 payload type 获取版本信息
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:20:01
 * @since 1.0
 */
public interface RevisionResolver {
	
	/**
	 * 返回指定版本
     *
     * @param payloadType The type for which to return the revision
     * @return the revision for the given <code>payloadType</code>
     */
    String revisionOf(Class<?> payloadType);
}
