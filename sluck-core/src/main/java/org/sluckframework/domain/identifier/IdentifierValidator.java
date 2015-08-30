package org.sluckframework.domain.identifier;

import java.util.Map;
import java.util.WeakHashMap;

import static org.sluckframework.common.util.ReflectionUtils.declaringClass;


/**标识符验证器， 用于验证指定的标识符class是否为合格的标识符
 * 标准的标识符 应该 重写 hashCocd() equals() toString()方法
 * 
 * @author sunxy
 * @time 2015年8月28日 下午12:43:30	
 * @since 1.0
 */
public class IdentifierValidator {
	private static final IdentifierValidator INSTANCE = new IdentifierValidator();
    private static final Object NULL = new Object();

    private final Map<Class<?>, Object> whiteList = new WeakHashMap<Class<?>, Object>();

    public static IdentifierValidator getInstance() {
        return INSTANCE;
    }

    /**
     * 验证标识符是否包含指定 需要重写的方法
     * @param aggregateIdentifierType The identifier to validate
     */
    public static void validateIdentifier(Class<?> aggregateIdentifierType) {
        if (!getInstance().isValidIdentifier(aggregateIdentifierType)) {
            throw new IllegalArgumentException("One of the events contains an unsuitable aggregate identifier "
                                                       + "Suspected class: " + aggregateIdentifierType.getName());
        }
    }

    private IdentifierValidator() {}

    public boolean isValidIdentifier(Class<?> identifierType) {
        if (!whiteList.containsKey(identifierType)) {
        	if (Object.class.equals(declaringClass(identifierType, "hashCode"))) {
                return false;
            }
        	if (Object.class.equals(declaringClass(identifierType, "equals"))) {
                return false;
            }
            if (Object.class.equals(declaringClass(identifierType, "toString"))) {
                return false;
            }
            whiteList.put(identifierType, NULL); //learn with hashSet
        }
        return true;
    }

}
