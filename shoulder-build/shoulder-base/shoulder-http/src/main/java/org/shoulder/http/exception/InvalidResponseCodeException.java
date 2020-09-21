package org.shoulder.http.exception;

/**
 * 响应值中 code 不为 0
 *
 * @author lym
 */
public class InvalidResponseCodeException extends InvalidResponseException {

    public InvalidResponseCodeException(Throwable cause) {
        super(cause);
    }
}
