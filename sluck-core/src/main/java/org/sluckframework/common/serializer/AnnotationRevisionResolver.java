package org.sluckframework.common.serializer;


/**
 * 注解 版本解析器
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:29:26
 * @since 1.0
 */
public class AnnotationRevisionResolver implements RevisionResolver {

	@Override
	public String revisionOf(Class<?> payloadType) {
		Revision revision = payloadType.getAnnotation(Revision.class);
		return revision != null ? revision.value() : null;
	}

}
