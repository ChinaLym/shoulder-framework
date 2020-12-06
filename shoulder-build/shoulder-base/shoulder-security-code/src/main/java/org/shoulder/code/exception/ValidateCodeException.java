package org.shoulder.code.exception;

import org.shoulder.core.exception.BaseRuntimeException;

/**
 * 验证码相关异常
 *
 * @author lym
 */
public class ValidateCodeException extends BaseRuntimeException {

    public ValidateCodeException(String msg) {
        super(msg);
    }

    public ValidateCodeException(String msg, Throwable e) {
        super(msg, e);
    }

}
