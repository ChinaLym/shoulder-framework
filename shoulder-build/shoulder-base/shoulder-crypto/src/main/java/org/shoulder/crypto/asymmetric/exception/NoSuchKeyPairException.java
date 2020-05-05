package org.shoulder.crypto.asymmetric.exception;

/**
 *	密钥对缺失错误
 * @author lym
 */
public class NoSuchKeyPairException extends KeyPairException {
	private static final long serialVersionUID = 6012356833048617406L;

	public NoSuchKeyPairException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchKeyPairException(String message) {
		super(message);
	}
}
