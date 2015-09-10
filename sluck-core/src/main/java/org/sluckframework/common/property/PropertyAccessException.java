package org.sluckframework.common.property;

import org.sluckframework.common.exception.SluckConfigurationException;

/**
 * Author: sunxy
 * Created: 2015-09-10 23:29
 * Since: 1.0
 */
public class PropertyAccessException extends SluckConfigurationException{

    private static final long serialVersionUID = 6847742543928756433L;

    public PropertyAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
