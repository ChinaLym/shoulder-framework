package org.shoulder.core.exception;

/**
 * json 序列化异常，非强制捕获
 *
 * @author lym
 */
public class JsonRuntimeException extends BaseRuntimeException {

    public JsonRuntimeException(Throwable cause) {
        super(cause);
    }

    public JsonRuntimeException(String message) {
        super(message);
    }

    public JsonRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonRuntimeException(String code, String message) {
        super(code, message);
    }

    public JsonRuntimeException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public JsonRuntimeException(String code, String message, Object... params) {
        super(code, message, params);
    }

}
