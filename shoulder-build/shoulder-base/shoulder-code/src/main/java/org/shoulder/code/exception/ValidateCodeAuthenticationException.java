package org.shoulder.code.exception;


import org.springframework.security.core.AuthenticationException;

/**
 * 验证码认证不通过
 * @author lym
 */
public class ValidateCodeAuthenticationException extends AuthenticationException {

	public ValidateCodeAuthenticationException(String msg) {
		super(msg);
	}

	public ValidateCodeAuthenticationException(String msg, Throwable e) {
		super(msg, e);
	}

}
