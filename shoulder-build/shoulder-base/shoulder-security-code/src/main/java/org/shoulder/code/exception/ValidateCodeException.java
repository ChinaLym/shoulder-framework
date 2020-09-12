package org.shoulder.code.exception;

/**
 * 验证码相关异常
 *
 * @author lym
 */
public class ValidateCodeException extends RuntimeException {

    public ValidateCodeException(String msg) {
        super(msg);
    }

    public ValidateCodeException(String msg, Throwable e) {
        super(msg, e);
    }

}
