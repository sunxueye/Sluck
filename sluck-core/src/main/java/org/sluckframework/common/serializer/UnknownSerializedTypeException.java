package org.sluckframework.common.serializer;

import static java.lang.String.format;

import org.sluckframework.common.exception.SluckNonTransientException;

/**
 * @author sunxy
 * @time 2015年8月29日 下午6:26:07
 * @since 1.0
 */
public class UnknownSerializedTypeException extends SluckNonTransientException {

	private static final long serialVersionUID = 4530073410619605780L;
	
    public UnknownSerializedTypeException(SerializedType serializedType) {
        super(format("Could not deserialize a message. The serialized type is unknown: %s (rev. %s)",
                     serializedType.getName(), serializedType.getRevision()));
    }

    public UnknownSerializedTypeException(SerializedType serializedType, Throwable cause) {
        super(format("Could not deserialize a message. The serialized type is unknown: %s (rev. %s)",
                     serializedType.getName(), serializedType.getRevision()),
              cause);
    }

}
