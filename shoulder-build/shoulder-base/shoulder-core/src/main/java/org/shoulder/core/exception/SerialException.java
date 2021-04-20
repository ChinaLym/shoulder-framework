package org.shoulder.core.exception;

/**
 * 序列化异常，非强制捕获
 * 由于这类异常及其可能为编码导致，故不应该报给前端暴露代码漏洞，应由开发排查
 *
 * @author lym
 */
public class SerialException extends BaseRuntimeException {

    private static final long serialVersionUID = 5330049957369887114L;

    public SerialException(Throwable cause) {
        super(cause);
    }

    public SerialException(String message) {
        super(message);
    }

    public SerialException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerialException(String code, String message) {
        super(code, message);
    }

    public SerialException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public SerialException(String code, String message, Object... params) {
        super(code, message, params);
    }

}
