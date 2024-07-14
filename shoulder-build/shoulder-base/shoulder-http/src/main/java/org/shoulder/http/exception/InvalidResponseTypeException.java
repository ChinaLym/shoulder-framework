package org.shoulder.http.exception;

import java.io.Serial;

/**
 * 响应格式不是 RestResult
 * - code、msg、data
 *
 * @author lym
 */
public class InvalidResponseTypeException extends InvalidResponseException {

    @Serial
    private static final long serialVersionUID = 5681540721974066752L;

    public InvalidResponseTypeException(Throwable cause) {
        super(cause);
    }
}
