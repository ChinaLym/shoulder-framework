package org.shoulder.crypto.exception;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.ErrorCode;

/**
 * 加解密相关运行时异常
 *
 * @author lym
 */
public class CipherRuntimeException extends BaseRuntimeException {

    private static final long serialVersionUID = -1391563921356126481L;

    public CipherRuntimeException(Throwable cause) {
        super(cause);
    }

    public CipherRuntimeException(String message) {
        super(message);
    }

    public CipherRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CipherRuntimeException(ErrorCode error) {
        super(error);
    }

    public CipherRuntimeException(ErrorCode error, Object... args) {
        super(error, args);
    }

    public CipherRuntimeException(ErrorCode error, Throwable t, Object... args) {
        super(error, t, args);
    }

    public CipherRuntimeException(String code, String message) {
        super(code, message);
    }

    public CipherRuntimeException(String code, String message, Throwable cause, Object... args) {
        super(code, message, cause, args);
    }

    public CipherRuntimeException(String code, String message, Object... args) {
        super(code, message, args);
    }
}
