package org.shoulder.http.exception;

/**
 * 响应格式不是 RestResult
 * - code、msg、data
 *
 * @author lym
 */
public class InvalidResponseTypeException extends InvalidResponseException {

    public InvalidResponseTypeException(Throwable cause) {
        super(cause);
    }
}
