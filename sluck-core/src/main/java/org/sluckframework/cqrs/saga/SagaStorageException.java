package org.sluckframework.cqrs.saga;

import org.sluckframework.common.exception.SluckTransientException;

/**
 * Author: sunxy
 * Created: 2015-09-13 15:02
 * Since: 1.0
 */
public class SagaStorageException extends SluckTransientException {

    private static final long serialVersionUID = 7980796051903841498L;

    public SagaStorageException(String message) {
        super(message);
    }

    public SagaStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
