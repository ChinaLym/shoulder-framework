package org.shoulder.crypto.negotiation.exception;

import org.shoulder.crypto.exception.CryptoException;

/**
 *	传输加解密出错 ECDH
 * @author lym
 */
public class TransportCryptoException extends CryptoException {

	public TransportCryptoException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
