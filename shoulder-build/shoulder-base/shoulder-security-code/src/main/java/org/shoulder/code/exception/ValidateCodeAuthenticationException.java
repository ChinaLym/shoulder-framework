package org.shoulder.code.exception;


import org.shoulder.security.exception.ShoulderAuthenticationException;

/**
 * 验证码认证不通过
 *
 * @author lym
 */
public class ValidateCodeAuthenticationException extends ShoulderAuthenticationException {

    public ValidateCodeAuthenticationException(String msg) {
        super(msg);
    }

    public ValidateCodeAuthenticationException(String msg, Throwable e) {
        super(msg, e);
    }

}
