package org.shoulder.crypto.aes.exception;

/**
 * AES 加解密出错
 *
 * @author lym
 */
public class AesCryptoException extends SymmetricCryptoException {

    public AesCryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AesCryptoException(String message) {
        super(message);
    }

}
