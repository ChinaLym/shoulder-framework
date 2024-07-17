package org.shoulder.crypto.asymmetric.exception;

import org.shoulder.crypto.exception.CryptoException;

import java.io.Serial;

/**
 * 非对称加解密出错
 *
 * @author lym
 */
public class AsymmetricCryptoException extends CryptoException {

    @Serial private static final long serialVersionUID = -6356869518676423610L;

    public AsymmetricCryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsymmetricCryptoException(String message) {
        super(message);
    }
}
