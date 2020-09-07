package org.shoulder.crypto.asymmetric.exception;

/**
 * 密钥对错误
 *
 * @author lym
 */
public class KeyPairException extends AsymmetricCryptoException {
    private static final long serialVersionUID = 6012356833048617406L;

    public KeyPairException(String message, Throwable cause) {
        super(message, cause);
    }


    public KeyPairException(String message) {
        super(message);
    }
}
