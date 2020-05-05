package org.shoulder.core.exception;

/**
 * json 序列化异常，非强制捕获
 *
 * @author lym
 */
public class JsonException extends BaseRuntimeException {

    public JsonException(Throwable cause) {
        super(cause);
    }

    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonException(String code, String message) {
        super(code, message);
    }

    public JsonException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public JsonException(String code, String message, Object... params) {
        super(code, message, params);
    }

}
