package org.shoulder.code.exception;

/**
 * 没有与这种 ValidateCodeType 验证码类型对应的处理器
 * @author lym
 */
public class NoSuchValidateCodeProcessorException extends ValidateCodeException {

	public NoSuchValidateCodeProcessorException(String msg) {
		super(msg);
	}

	public NoSuchValidateCodeProcessorException(String msg, Throwable e) {
		super(msg, e);
	}

}
