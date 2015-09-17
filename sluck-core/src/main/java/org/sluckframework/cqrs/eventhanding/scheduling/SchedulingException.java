package org.sluckframework.cqrs.eventhanding.scheduling;

import org.sluckframework.common.exception.SluckTransientException;

/**
 * Author: sunxy
 * Created: 2015-09-17 22:09
 * Since: 1.0
 */
public class SchedulingException extends SluckTransientException {

    private static final long serialVersionUID = 5986001762933823599L;

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}
