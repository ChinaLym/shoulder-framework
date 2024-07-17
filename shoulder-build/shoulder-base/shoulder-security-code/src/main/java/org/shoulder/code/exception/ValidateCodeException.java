package org.shoulder.code.exception;

import org.shoulder.core.exception.BaseRuntimeException;

import java.io.Serial;

/**
 * 验证码相关异常
 *
 * @author lym
 */
public class ValidateCodeException extends BaseRuntimeException {

    @Serial private static final long serialVersionUID = 265959031127279876L;

    public ValidateCodeException(String msg) {
        super(msg);
    }

    public ValidateCodeException(String msg, Throwable e) {
        super(msg, e);
    }

}
