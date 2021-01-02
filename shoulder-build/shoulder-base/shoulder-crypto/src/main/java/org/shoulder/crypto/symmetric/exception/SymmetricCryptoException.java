package org.shoulder.crypto.symmetric.exception;

import org.shoulder.crypto.exception.CryptoException;

/**
 * 对称加解密出错
 *
 * @author lym
 */
public class SymmetricCryptoException extends CryptoException {

    public SymmetricCryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public SymmetricCryptoException(String message) {
        super(message);
    }

}
