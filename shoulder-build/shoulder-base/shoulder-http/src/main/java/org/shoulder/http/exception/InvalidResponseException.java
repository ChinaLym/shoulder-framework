package org.shoulder.http.exception;

import org.shoulder.core.exception.BaseRuntimeException;

/**
 * 响应不是 RestResult
 *
 * @author lym
 */
public class InvalidResponseException extends BaseRuntimeException {

    private static final long serialVersionUID = -1935896109628796740L;

    public InvalidResponseException(Throwable cause) {
        super(cause);
    }
}
