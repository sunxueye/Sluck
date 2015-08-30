package org.sluckframework.domain.identifier;

import org.sluckframework.domain.valueobject.ValueObject;


/**
 * @author sunxy
 * @time 2015年8月28日 上午11:39:25	
 * @since 1.0
 */
public interface Identifier <T> extends ValueObject{
	
	T getIdentifier();

}
