package org.shoulder.http.exception;

import org.shoulder.core.exception.BaseRuntimeException;

/**
 * 响应不是 BaseResponse<?>
 *
 * @author lym
 */
public class InvalidResponseException extends BaseRuntimeException {

    public InvalidResponseException(Throwable cause) {
        super(cause);
    }
}
